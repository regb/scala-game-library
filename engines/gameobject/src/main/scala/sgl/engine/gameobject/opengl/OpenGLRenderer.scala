package sgl.engine.gameobject.opengl

import scala.collection.mutable
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

import sgl.{ClipRect, OpenGLProvider, PortableFont, SystemProvider, VertexBatch, WindowProvider}
import sgl.engine.gameobject.{ClearColor, Color, CompositionError, Scope, World}
import sgl.assets.{AssetChange, AssetChangeListener, AssetReloads, DrawableAsset, RawImageAsset, TextureFilter}
import sgl.engine.gameobject.render2d.{Baked2D, Camera2D, Painter2D, Render2D, Texture2D}
import sgl.engine.gameobject.render3d._
import sgl.geometry.{Mat4, Vec2, Vec3}
import sgl.util.{DefaultLoader, Loader}

/** @param reloads hot-reloaded images are uploaded again and swapped under
  *   the textures that show them. Disabled by default.
  */
final class OpenGLRenderer(
    private val app: OpenGLProvider with SystemProvider with WindowProvider,
    reloads: AssetReloads = AssetReloads.Disabled
) extends Renderer {
  private val GL = app.OpenGL

  private type GLProgram = GL.Program
  private type GLTexture = GL.Texture
  private type GLVertexArray = GL.VertexArray
  private type GLBuffer = GL.Buffer
  private type GLUniformLocation = GL.UniformLocation

  private sealed trait MeshState
  private final case class MeshReady(vao: GLVertexArray, vertexBuffer: GLBuffer, indexBuffer: GLBuffer, indexCount: Int) extends MeshState
  private final case class MeshFailed(cause: Throwable) extends MeshState
  private case object MeshLoading extends MeshState
  private case object MeshReleased extends MeshState
  private final case class GLMaterial(texture: Texture2D, emissive: Float)

  /** One uploaded image: a standalone drawable or raw image, or a texture-atlas page. */
  private final case class PageKey(resourceName: String, filter: TextureFilter, atlasPage: Boolean)
  private sealed trait PageState
  private final case class PageLoading(loader: Loader[GLTexture]) extends PageState
  private final case class PageReady(texture: GLTexture) extends PageState
  private final case class PageFailed(cause: Throwable) extends PageState

  /** Where an image lives inside its page: `pageU = uOffset + u * uScale`, kept
    * between the minimum and maximum, which are the middles of its edge texels
    * in an atlas.
    *
    * An image that is a page of its own uses the identity. Texture coordinates
    * stay relative to the image, so crops and flips compose unchanged.
    */
  private final case class ImageView(
      page: PageKey,
      uOffset: Float = 0f, vOffset: Float = 0f, uScale: Float = 1f, vScale: Float = 1f,
      uMin: Float = 0f, vMin: Float = 0f, uMax: Float = 1f, vMax: Float = 1f
  ) {
    def u(local: Float): Float = uOffset + local * uScale
    def v(local: Float): Float = vOffset + local * vScale

    /** The above for the mesh shader, as the two used columns of a matrix. */
    val region: Array[Float] = Array(
      uOffset, vOffset, uScale, vScale,
      uMin, vMin, uMax, vMax,
      0f, 0f, 0f, 0f,
      0f, 0f, 0f, 0f
    )
  }
  private final case class ResolvedTexture(texture: GLTexture, view: ImageView)

  private val program: GLProgram = createProgram()
  // Textures one draw of shapes can mix.
  private val textureSlots: Int = scala.math.min(VertexBatch.MaxSlots, GL.maxTextureUnits)

  // A program painting shapes, whose vertices are a position, a color, texture
  // coordinates, and the slot of their texture: 0 for none, then from 1.
  private final class PaintProgram(val program: GLProgram, val positionSize: Int) {
    val floatsPerVertex: Int = positionSize + 7
    val position: GL.AttributeLocation = requireAttribute(program, "a_position")
    val color: GL.AttributeLocation = requireAttribute(program, "a_color")
    val uv: GL.AttributeLocation = requireAttribute(program, "a_uv")
    val slot: GL.AttributeLocation = requireAttribute(program, "a_slot")
    val batch = new VertexBatch[GLTexture](floatsPerVertex, textureSlots)

    GL.useProgram(program)
    (0 until textureSlots).foreach(i => GL.uniform1i(requireUniform(program, s"u_texture$i"), i))
    GL.useNoProgram()
  }
  private val paint2D = new PaintProgram(createPaintProgram(
    "2D", "vec2",
    // A scale and an offset in the first column, and a color in the second.
    "uniform mat4 u_placement;",
    "vec4(a_position * u_placement[0].xy + u_placement[0].zw, 0.0, 1.0)",
    "a_color * u_placement[1]"
  ), positionSize = 2)
  private val paint3D = new PaintProgram(createPaintProgram(
    "3D", "vec3",
    "uniform mat4 u_viewProjection;",
    "u_viewProjection * vec4(a_position, 1.0)",
    "a_color"
  ), positionSize = 3)
  private val mvpLocation: GLUniformLocation = requireUniform(program, "u_mvp")
  private val modelLocation: GLUniformLocation = requireUniform(program, "u_model")
  private val textureLocation: GLUniformLocation = requireUniform(program, "u_texture")
  private val emissiveLocation: GLUniformLocation = requireUniform(program, "u_emissive")
  private val regionLocation: GLUniformLocation = requireUniform(program, "u_region")
  private val viewProjection3DLocation: GLUniformLocation = requireUniform(paint3D.program, "u_viewProjection")
  private val placementLocation: GLUniformLocation = requireUniform(paint2D.program, "u_placement")
  private val vao2D: GLVertexArray = GL.genVertexArray()
  private val vbo2D: GLBuffer = GL.genBuffer()
  // Buffers of the painted 3D drawings, made by the first frame that has any.
  private var buffers3D: Option[(GLVertexArray, GLBuffer)] = None

  private val meshes = mutable.ArrayBuffer.empty[MeshState]
  // What each texture shows, until it is released.
  private val textures = mutable.ArrayBuffer.empty[Option[ImageView]]
  // Pages own the GPU textures, and go with the last texture that shows them.
  private val pages = mutable.HashMap.empty[PageKey, PageState]
  private val pageUses = mutable.HashMap.empty[PageKey, Int]
  // Only the latest upload of a page is kept; older ones finishing late are deleted.
  private val pageGenerations = mutable.HashMap.empty[PageKey, Int]
  private val maxTextureSize: Int = GL.maxTextureSize
  private val materials = mutable.ArrayBuffer.empty[Option[GLMaterial]]
  private var disposed = false

  GL.enable(GL.DepthTest)

  private object imageReloads extends AssetChangeListener {
    override def assetsChanged(change: AssetChange): Unit = reloadImages(change)
  }
  private val stopReloading: () => Unit = reloads.subscribe(imageReloads)

  // What is created in a scope is released with it.
  private def owned[A](scope: Scope)(create: => A)(release: A => Unit): A = {
    ensureActive()
    if(!scope.isActive) throw new CompositionError(s"Cannot create an asset in inactive scope ${scope.name}")
    val asset = create
    scope.onDestroy(() => release(asset))
    asset
  }

  override def createMesh(data: MeshData)(using scope: Scope): Mesh = owned(scope) {
    val id = meshes.size
    meshes += MeshLoading
    app.runOnOpenGLThread {
      if(!disposed && meshes(id) == MeshLoading) {
        try meshes(id) = uploadMesh(data)
        catch { case t: Throwable => if(meshes(id) != MeshReleased) meshes(id) = MeshFailed(t) }
      }
    }
    new Mesh(id)
  }(releaseMesh)

  private def uploadMesh(data: MeshData): MeshReady = {
    GL.useProgram(program)

    val vertexArray = GL.genVertexArray()
    GL.bindVertexArray(vertexArray)

    val vbo = GL.genBuffer()
    GL.bindBuffer(GL.ArrayBuffer, vbo)
    GL.bufferDataFloat(GL.ArrayBuffer, data.vertices, GL.StaticDraw)

    val ibo = GL.genBuffer()
    GL.bindBuffer(GL.ElementArrayBuffer, ibo)
    GL.bufferDataShort(GL.ElementArrayBuffer, data.indices, GL.StaticDraw)

    val stride = 8 * 4
    val positionLocation = requireAttribute(program, "a_position")
    GL.enableVertexAttribArray(positionLocation)
    GL.vertexAttribPointer(positionLocation, 3, GL.FloatType, normalized = false, stride, 0L)

    val normalLocation = requireAttribute(program, "a_normal")
    GL.enableVertexAttribArray(normalLocation)
    GL.vertexAttribPointer(normalLocation, 3, GL.FloatType, normalized = false, stride, 3L * 4L)

    val uvLocation = requireAttribute(program, "a_uv")
    GL.enableVertexAttribArray(uvLocation)
    GL.vertexAttribPointer(uvLocation, 2, GL.FloatType, normalized = false, stride, 6L * 4L)

    GL.unbindVertexArray()
    MeshReady(vertexArray, vbo, ibo, data.indices.length)
  }

  override def loadTexture2D(asset: DrawableAsset)(using Scope): Texture2D = loadView(viewOf(asset))
  override def loadTexture2D(asset: RawImageAsset)(using Scope): Texture2D =
    loadView(ImageView(PageKey(asset.resourceName, TextureFilter.Nearest, atlasPage = false)))

  private def loadView(view: ImageView)(using scope: Scope): Texture2D = owned(scope) {
    val id = textures.size
    textures += Some(view)
    pageUses(view.page) = pageUses.getOrElse(view.page, 0) + 1
    if(!pages.contains(view.page)) { val _ = uploadPage(view.page) }
    new Texture2D(id)
  }(releaseTexture)

  override def whenReady(texture: Texture2D): Loader[Texture2D] = viewOf(texture) match {
    case Some(view) => pageLoader(view.page).map(_ => texture)
    case None => Loader.failed(new IllegalStateException("The texture has been released"))
  }

  private def viewOf(texture: Texture2D): Option[ImageView] =
    if(texture.id >= 0 && texture.id < textures.size) textures(texture.id) else None

  override def createMaterial(texture: Texture2D, emissive: Float)(using scope: Scope): Material = owned(scope) {
    val id = materials.size
    materials += Some(GLMaterial(texture, emissive))
    new Material(id)
  }(releaseMaterial)

  override def releaseMesh(mesh: Mesh): Unit = {
    if(mesh.id < 0 || mesh.id >= meshes.size) return
    meshes(mesh.id) match {
      case ready: MeshReady =>
        GL.deleteBuffer(ready.indexBuffer)
        GL.deleteBuffer(ready.vertexBuffer)
        GL.deleteVertexArray(ready.vao)
      case _ => ()
    }
    meshes(mesh.id) = MeshReleased
  }

  override def releaseTexture(texture: Texture2D): Unit = viewOf(texture).foreach { view =>
    textures(texture.id) = None
    val uses = pageUses(view.page) - 1
    if(uses > 0) pageUses(view.page) = uses
    else {
      pageUses.remove(view.page)
      pages.remove(view.page).foreach {
        case PageReady(handle) => GL.deleteTexture(handle)
        case _ => ()
      }
      // An upload still under way is no longer the latest, and is deleted.
      pageGenerations(view.page) = pageGenerations.getOrElse(view.page, 0) + 1
    }
  }

  override def releaseMaterial(material: Material): Unit = {
    if(material.id >= 0 && material.id < materials.size) materials(material.id) = None
  }

  override def resize(width: Int, height: Int): Unit =
    GL.viewport(0, 0, width, height)

  // World drawings come first, ordered by z; screen drawings follow, ordered
  // by their order. Drawings that compare equal keep their submission order.
  private final case class Queued2D(screen: Boolean, key: Float, draw: Painter2D => Unit)
  private val queue2D = mutable.ArrayBuffer.empty[Queued2D]

  override val render2D: Render2D = new Render2D {
    override def world(z: Float)(draw: Painter2D => Unit): Unit =
      queue2D += Queued2D(screen = false, z, draw)
    override def screen(order: Int)(draw: Painter2D => Unit): Unit =
      queue2D += Queued2D(screen = true, order.toFloat, draw)
  }

  private final case class QueuedMesh(mesh: Mesh, material: Material, transform: Mat4)
  private final case class Painted3D(at: Vec3, draw: Painter3D => Unit)
  private val queueMeshes = mutable.ArrayBuffer.empty[QueuedMesh]
  private val queue3D = mutable.ArrayBuffer.empty[Painted3D]

  override val render3D: Render3D = new Render3D {
    override def mesh(mesh: Mesh, material: Material, transform: Mat4): Unit =
      queueMeshes += QueuedMesh(mesh, material, transform)
    override def painted(at: Vec3)(draw: Painter3D => Unit): Unit =
      queue3D += Painted3D(at, draw)
  }

  private def clearQueues(): Unit = {
    queue2D.clear()
    queueMeshes.clear()
    queue3D.clear()
  }

  // Glyphs of the built-in font, uploaded by the first text drawn with them.
  private val fontTextures = mutable.HashMap.empty[Boolean, GLTexture]

  private def fontTexture(bold: Boolean): GLTexture = fontTextures.getOrElseUpdate(bold, {
    val face = PortableFont.face(bold)
    GL.activeTexture(GL.Texture0)
    val texture = GL.createTextureImage2D(face.atlasWidth, face.atlasHeight, face.rgba).texture
    GL.bindTexture(GL.Texture2D, texture)
    GL.texParameteri(GL.Texture2D, GL.TextureMinFilter, GL.Linear)
    GL.texParameteri(GL.Texture2D, GL.TextureMagFilter, GL.Linear)
    GL.unbindTexture(GL.Texture2D)
    texture
  })

  /** A drawing on the GPU: its vertices, and the runs of them that share textures. */
  private final class Compiled(
      val vertexArray: GLVertexArray,
      val buffer: GLBuffer,
      val runs: Vector[CompiledRun],
      // The images it shows, to notice that one has changed.
      val pages: Vector[(PageKey, Option[GLTexture])]
  )
  private final case class CompiledRun(textures: Vector[GLTexture], first: Int, count: Int)
  private val bakedDrawings = mutable.HashSet.empty[Baked2D]

  override def bake2D(draw: Painter2D => Unit)(using scope: Scope): Baked2D = owned(scope) {
    val baked = new Baked2D(draw)
    bakedDrawings += baked
    baked
  }(releaseBaked)

  override def releaseBaked(baked: Baked2D): Unit =
    if(bakedDrawings.remove(baked)) deleteCompiled(baked)

  private def deleteCompiled(baked: Baked2D): Unit = baked.compiled match {
    case compiled: Compiled =>
      GL.deleteBuffer(compiled.buffer)
      GL.deleteVertexArray(compiled.vertexArray)
      baked.compiled = null
    case _ => ()
  }

  private def loadedPage(key: PageKey): Option[GLTexture] = pages.get(key) match {
    case Some(PageReady(handle)) => Some(handle)
    case _ => None
  }

  // Filled by one baked drawing at a time, while the batch of the frame is
  // being filled too.
  private lazy val bakeBatch = new Batch(paint2D.floatsPerVertex, textureSlots)

  // Leaves another vertex array and buffer bound than before: the batch of the
  // frame binds its own when it is drawn.
  private def compile(baked: Baked2D): Compiled = {
    deleteCompiled(baked)
    baked.stale = false
    bakeBatch.clear()
    val shown = mutable.LinkedHashSet.empty[PageKey]
    baked.draw(new Painter(
      bakeBatch, left = -1f, top = 1f, space = Vec2(2f, -2f),
      camera = Vec2(0f, 0f), viewport = Vec2(1f, 1f), screenSize = Vec2(1f, 1f), baking = Some(shown)
    ))

    val vertexArray = GL.genVertexArray()
    val buffer = GL.genBuffer()
    GL.bindVertexArray(vertexArray)
    GL.bindBuffer(GL.ArrayBuffer, buffer)
    describeVertices(paint2D)
    if(bakeBatch.length > 0) GL.bufferDataFloat(GL.ArrayBuffer, bakeBatch.values, bakeBatch.length, GL.StaticDraw)
    GL.unbindVertexArray()

    val runs = Vector.newBuilder[CompiledRun]
    var textures = Vector.empty[GLTexture]
    bakeBatch.foreachRun((_, texture) => textures :+= texture) { (first, count) =>
      if(count > 0) runs += CompiledRun(textures, first, count)
      textures = Vector.empty
    }
    bakeBatch.clear()
    val compiled = new Compiled(vertexArray, buffer, runs.result(), shown.iterator.map(key => key -> loadedPage(key)).toVector)
    baked.compiled = compiled
    compiled
  }

  private def compiledFor(baked: Baked2D): Compiled = baked.compiled match {
    case compiled: Compiled if !baked.stale && compiled.pages.forall { case (key, texture) => loadedPage(key) == texture } => compiled
    case _ => compile(baked)
  }

  // What a texture shows does not change while a frame is drawn, so it is only
  // looked up once for it: drawings switch textures all the time. The texture
  // is None as long as it has not loaded.
  private final class Shown(val view: ImageView, val texture: Option[GLTexture])
  private var shownFrame = 0
  private var shownAt = new Array[Int](0)
  private var shownTextures = new Array[Shown](0)

  /** What a texture shows in this frame, or null when it was released. */
  private def shown(texture: Texture2D): Shown = {
    val id = texture.id
    if(id < 0 || id >= textures.size) null
    else {
      if(id >= shownAt.length) {
        val size = scala.math.max(textures.size, shownAt.length * 2)
        shownAt = java.util.Arrays.copyOf(shownAt, size)
        shownTextures = java.util.Arrays.copyOf(shownTextures, size)
      }
      if(shownAt(id) != shownFrame) {
        shownAt(id) = shownFrame
        shownTextures(id) = textures(id) match {
          case Some(view) => new Shown(view, resolve(texture).map(_.texture))
          case None => null
        }
      }
      shownTextures(id)
    }
  }

  /** Turns points of one space into batched vertices. The top left and the size
    * of the screen are given in that space: its edges end up at -1 and 1.
    *
    * @param baking for a drawing that is kept, where to note the images it shows
    */
  private final class Painter(
      batch: Batch,
      left: Float, top: Float, space: Vec2,
      override val camera: Vec2,
      override val viewport: Vec2,
      override val screenSize: Vec2,
      baking: Option[mutable.Set[PageKey]] = None
  ) extends Painter2D {
    private val right = left + space.x
    private val bottom = top + space.y
    private val scaleX = 2f / space.x
    private val scaleY = -2f / space.y
    private val offsetX = -1f - left * scaleX
    private val offsetY = 1f - top * scaleY
    private var addedSlots = 0f
    private var currentClip: Option[ClipRect] = None

    // Maps are often much larger than what is shown of them. Where a drawing
    // that is kept will be is not known.
    private def offScreen(a: Vec2, b: Vec2, c: Vec2, d: Vec2): Boolean = baking.isEmpty && (
      (a.x < left && b.x < left && c.x < left && d.x < left) ||
        (a.x > right && b.x > right && c.x > right && d.x > right) ||
        (a.y < top && b.y < top && c.y < top && d.y < top) ||
        (a.y > bottom && b.y > bottom && c.y > bottom && d.y > bottom)
    )

    // A rectangle by its center and half sides, which may be negative.
    private def offScreen(x: Float, y: Float, halfWidth: Float, halfHeight: Float): Boolean = {
      val w = scala.math.abs(halfWidth)
      val h = scala.math.abs(halfHeight)
      baking.isEmpty && (x + w < left || x - w > right || y + h < top || y - h > bottom)
    }

    private def vertex(x: Float, y: Float, color: Color, u: Float, v: Float): Unit = {
      val out = batch.values
      val offset = batch.next()
      out(offset) = x * scaleX + offsetX
      out(offset + 1) = y * scaleY + offsetY
      out(offset + 2) = color.red
      out(offset + 3) = color.green
      out(offset + 4) = color.blue
      out(offset + 5) = color.alpha
      out(offset + 6) = u
      out(offset + 7) = v
      out(offset + 8) = batch.slot + addedSlots
    }
    private def vertex(p: Vec2, color: Color, u: Float, v: Float): Unit = vertex(p.x, p.y, color, u, v)

    /** What a texture shows, or null as long as there is nothing to show. */
    private def use(texture: Texture2D): Shown = {
      val image = shown(texture)
      if(image == null) null
      else {
        if(baking.nonEmpty) baking.foreach(_ += image.view.page)
        if(image.texture.isEmpty) null else image
      }
    }

    override def triangle(a: Vec2, b: Vec2, c: Vec2, color: Color): Unit = {
      batch.begin(None, 3)
      vertex(a, color, 0f, 0f)
      vertex(b, color, 0f, 0f)
      vertex(c, color, 0f, 0f)
    }

    override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit =
      textured(None, a, b, c, d, 0f, 0f, 0f, 0f, color)

    override def image(
        texture: Texture2D,
        a: Vec2, b: Vec2, c: Vec2, d: Vec2,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: Color
    ): Unit = {
      val image = use(texture)
      if(image == null) textured(None, a, b, c, d, u0, v0, u1, v1, color)
      else {
        val view = image.view
        textured(image.texture, a, b, c, d, view.u(u0), view.v(v0), view.u(u1), view.v(v1), color)
      }
    }

    override def clip(position: Vec2, size: Vec2)(body: => Unit): Unit = {
      if(baking.nonEmpty) throw new CompositionError("A baked drawing cannot be clipped: clip where it is drawn")
      def pixelX(x: Float): Float = (x - left) * (screenSize.x / space.x)
      def pixelY(y: Float): Float = (y - top) * (screenSize.y / space.y)
      val inner = ClipRect(
        pixelX(scala.math.min(position.x, position.x + size.x)),
        pixelY(scala.math.min(position.y, position.y + size.y)),
        pixelX(scala.math.max(position.x, position.x + size.x)),
        pixelY(scala.math.max(position.y, position.y + size.y))
      )
      val outer = currentClip
      setClip(Some(outer.fold(inner)(_.intersect(inner))))
      try body
      finally setClip(outer)
    }

    private def setClip(clip: Option[ClipRect]): Unit = {
      // What was drawn so far was drawn in the other clip.
      flush(batch, vao2D, vbo2D)
      currentClip = clip
      ClipRect.set(GL, clip, app.Window.width, app.Window.height)
    }

    override def text(value: String, position: Vec2, size: Float, color: Color, bold: Boolean): Unit = {
      val face = PortableFont.face(bold)
      val texture = Some(fontTexture(bold))
      val scale = size / PortableFont.SourcePixelSize
      val baseline = position.y + face.ascent * scale
      var cursor = position.x
      PortableFont.foreachCodePoint(value) { codePoint =>
        val glyph = face.glyph(codePoint)
        if(glyph.width > 0 && glyph.height > 0) {
          val x0 = cursor + glyph.bearingX * scale
          val y0 = baseline + glyph.bearingY * scale
          box(
            texture, x0, y0, glyph.width * scale, glyph.height * scale,
            glyph.x.toFloat / face.atlasWidth, glyph.y.toFloat / face.atlasHeight,
            (glyph.x + glyph.width).toFloat / face.atlasWidth, (glyph.y + glyph.height).toFloat / face.atlasHeight,
            color, color, color, color
          )
        }
        cursor += glyph.advance * scale
      }
    }

    private def textured(
        texture: Option[GLTexture],
        a: Vec2, b: Vec2, c: Vec2, d: Vec2,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: Color
    ): Unit = if(!offScreen(a, b, c, d)) {
      batch.begin(texture, 6)
      vertex(a, color, u0, v0)
      vertex(b, color, u1, v0)
      vertex(c, color, u1, v1)
      vertex(a, color, u0, v0)
      vertex(c, color, u1, v1)
      vertex(d, color, u0, v1)
    }

    override def sprite(
        texture: Texture2D,
        x: Float, y: Float, width: Float, height: Float,
        rotation: Float,
        color: Color,
        u0: Float, v0: Float, u1: Float, v1: Float
    ): Unit = {
      val image = use(texture)
      val left = if(image == null) 0f else image.view.u(u0)
      val top = if(image == null) 0f else image.view.v(v0)
      val right = if(image == null) 0f else image.view.u(u1)
      val bottom = if(image == null) 0f else image.view.v(v1)
      // The half sides, turned.
      val cos = if(rotation == 0f) 1f else scala.math.cos(rotation.toDouble).toFloat
      val sin = if(rotation == 0f) 0f else scala.math.sin(rotation.toDouble).toFloat
      val ax = width / 2f * cos
      val ay = width / 2f * sin
      val bx = -height / 2f * sin
      val by = height / 2f * cos
      if(offScreen(x, y, scala.math.abs(ax) + scala.math.abs(bx), scala.math.abs(ay) + scala.math.abs(by))) return
      batch.begin(if(image == null) None else image.texture, 6)
      vertex(x - ax - bx, y - ay - by, color, left, top)
      vertex(x + ax - bx, y + ay - by, color, right, top)
      vertex(x + ax + bx, y + ay + by, color, right, bottom)
      vertex(x - ax - bx, y - ay - by, color, left, top)
      vertex(x + ax + bx, y + ay + by, color, right, bottom)
      vertex(x - ax + bx, y - ay + by, color, left, bottom)
    }

    override def rect(x: Float, y: Float, width: Float, height: Float, color: Color): Unit =
      box(None, x, y, width, height, 0f, 0f, 0f, 0f, color, color, color, color)

    override def shaded(
        texture: Texture2D,
        x: Float, y: Float, width: Float, height: Float,
        topLeft: Color, topRight: Color, bottomRight: Color, bottomLeft: Color,
        u0: Float, v0: Float, u1: Float, v1: Float
    ): Unit = {
      val image = use(texture)
      if(image == null) box(None, x, y, width, height, 0f, 0f, 0f, 0f, topLeft, topRight, bottomRight, bottomLeft)
      else {
        val view = image.view
        box(image.texture, x, y, width, height, view.u(u0), view.v(v0), view.u(u1), view.v(v1), topLeft, topRight, bottomRight, bottomLeft)
      }
    }

    private def box(
        texture: Option[GLTexture],
        x: Float, y: Float, width: Float, height: Float,
        u0: Float, v0: Float, u1: Float, v1: Float,
        topLeft: Color, topRight: Color, bottomRight: Color, bottomLeft: Color
    ): Unit = if(!offScreen(x + width / 2f, y + height / 2f, width / 2f, height / 2f)) {
      batch.begin(texture, 6)
      val x1 = x + width
      val y1 = y + height
      vertex(x, y, topLeft, u0, v0)
      vertex(x1, y, topRight, u1, v0)
      vertex(x1, y1, bottomRight, u1, v1)
      vertex(x, y, topLeft, u0, v0)
      vertex(x1, y1, bottomRight, u1, v1)
      vertex(x, y1, bottomLeft, u0, v1)
    }

    override def line(x0: Float, y0: Float, x1: Float, y1: Float, thickness: Float, color: Color): Unit = {
      val dx = x1 - x0
      val dy = y1 - y0
      val length = scala.math.sqrt((dx * dx + dy * dy).toDouble).toFloat
      if(length > 0f) {
        val nx = -dy / length * thickness / 2f
        val ny = dx / length * thickness / 2f
        val w = scala.math.abs(dx) + scala.math.abs(nx) * 2f
        val h = scala.math.abs(dy) + scala.math.abs(ny) * 2f
        if(offScreen((x0 + x1) / 2f, (y0 + y1) / 2f, w / 2f, h / 2f)) return
        batch.begin(None, 6)
        vertex(x0 + nx, y0 + ny, color, 0f, 0f)
        vertex(x1 + nx, y1 + ny, color, 0f, 0f)
        vertex(x1 - nx, y1 - ny, color, 0f, 0f)
        vertex(x0 + nx, y0 + ny, color, 0f, 0f)
        vertex(x1 - nx, y1 - ny, color, 0f, 0f)
        vertex(x0 - nx, y0 - ny, color, 0f, 0f)
      }
    }

    override def additive(draw: => Unit): Unit = {
      val before = addedSlots
      addedSlots = VertexBatch.AdditiveSlots
      try draw
      finally addedSlots = before
    }

    override def baked(baked: Baked2D, x: Float, y: Float, scale: Float, tint: Color): Unit = {
      if(baking.nonEmpty) throw new CompositionError("A baked drawing cannot draw another one")
      if(bakedDrawings.contains(baked)) {
        val compiled = compiledFor(baked)
        val placement = placementOf(scale * scaleX, scale * scaleY, x * scaleX + offsetX, y * scaleY + offsetY, tint)
        if(compiled.runs.nonEmpty) batch.between(() => drawCompiled(compiled, placement))
      }
    }
  }

  // Batches vertices in world units; the shader projects them.
  private final class WorldPainter(
      batch: Batch,
      override val camera: Vec3,
      override val right: Vec3,
      override val up: Vec3
  ) extends Painter3D {
    private def put(p: Vec3, color: Color, u: Float, v: Float): Unit = {
      val out = batch.values
      val offset = batch.next()
      out(offset) = p.x
      out(offset + 1) = p.y
      out(offset + 2) = p.z
      out(offset + 3) = color.red
      out(offset + 4) = color.green
      out(offset + 5) = color.blue
      out(offset + 6) = color.alpha
      out(offset + 7) = u
      out(offset + 8) = v
      out(offset + 9) = batch.slot
    }

    override def triangle(a: Vec3, b: Vec3, c: Vec3, color: Color): Unit = {
      batch.begin(None, 3)
      put(a, color, 0f, 0f)
      put(b, color, 0f, 0f)
      put(c, color, 0f, 0f)
    }

    override def quad(a: Vec3, b: Vec3, c: Vec3, d: Vec3, color: Color): Unit =
      textured(None, a, b, c, d, 0f, 0f, 0f, 0f, color)

    override def image(
        texture: Texture2D,
        a: Vec3, b: Vec3, c: Vec3, d: Vec3,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: Color
    ): Unit =
      resolve(texture) match {
        case Some(resolved) =>
          val view = resolved.view
          textured(Some(resolved.texture), a, b, c, d, view.u(u0), view.v(v0), view.u(u1), view.v(v1), color)
        case None =>
          textured(None, a, b, c, d, u0, v0, u1, v1, color)
      }

    private def textured(
        texture: Option[GLTexture],
        a: Vec3, b: Vec3, c: Vec3, d: Vec3,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: Color
    ): Unit = {
      batch.begin(texture, 6)
      put(a, color, u0, v0)
      put(b, color, u1, v0)
      put(c, color, u1, v1)
      put(a, color, u0, v0)
      put(c, color, u1, v1)
      put(d, color, u0, v1)
    }
  }

  override def beginFrame(world: World): Unit = {
    clearQueues()
    if(disposed) return

    val clear = world.globals.getOption[ClearColor]
      .getOrElse(ClearColor(0f, 0f, 0f, 1f))
    GL.clearColor(clear.red, clear.green, clear.blue, clear.alpha)
    GL.clear(GL.clearMask(GL.ColorBufferBit, GL.DepthBufferBit))
  }

  override def endFrame(world: World): Unit = {
    shownFrame += 1
    try {
      if(!disposed) {
        if(queueMeshes.nonEmpty || queue3D.nonEmpty) draw3D(world)
        if(queue2D.nonEmpty) draw2D(world)
      }
    } finally clearQueues()
  }

  private def draw3D(world: World): Unit = findCamera(world).foreach { camera =>
    val aspect = app.Window.width.toFloat / scala.math.max(1, app.Window.height).toFloat
    val projection = Mat4.perspective(camera.fovRadians, aspect, camera.near, camera.far)
    val placed = camera.spatial.globalMatrix.values
    val viewProjection = projection * camera.viewMatrix

    if(queueMeshes.nonEmpty) {
      GL.useProgram(program)
      GL.activeTexture(GL.Texture0)
      GL.uniform1i(textureLocation, 0)
      queueMeshes.foreach(drawMesh(viewProjection, _))
      GL.unbindVertexArray()
      GL.unbindTexture(GL.Texture2D)
      GL.useNoProgram()
    }

    if(queue3D.nonEmpty) {
      // The columns of the camera's transform are its axes and its position.
      def column(i: Int) = Vec3(placed(i * 4), placed(i * 4 + 1), placed(i * 4 + 2))
      val position = column(3)
      val farthestFirst = queue3D.toVector.sortBy(drawing => -(drawing.at - position).length)

      val (vertexArray, buffer) = buffers3D.getOrElse {
        val created = (GL.genVertexArray(), GL.genBuffer())
        buffers3D = Some(created)
        created
      }
      // Meshes hide what is behind them; the drawings themselves blend.
      GL.depthMask(write = false)
      try {
        paintWith(paint3D, vertexArray, buffer) { batch =>
          GL.uniformMatrix4fv(viewProjection3DLocation, transpose = false, viewProjection.values)
          val painter = new WorldPainter(batch, position, column(0).normalized, column(1).normalized)
          farthestFirst.foreach(_.draw(painter))
        }
      } finally GL.depthMask(write = true)
    }
  }

  // Sets up blending and the vertex layout, shared by 2D and 3D, around `draw`.
  private def paintWith(paint: PaintProgram, vertexArray: GLVertexArray, buffer: GLBuffer)(draw: Batch => Unit): Unit = {
    GL.enable(GL.Blend)
    // Colors come out of the shader multiplied by their alpha.
    GL.blendFunc(GL.One, GL.OneMinusSrcAlpha)
    GL.useProgram(paint.program)
    GL.bindVertexArray(vertexArray)
    GL.bindBuffer(GL.ArrayBuffer, buffer)
    describeVertices(paint)

    paint.batch.clear()
    draw(paint.batch)
    flush(paint.batch, vertexArray, buffer)

    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindVertexArray()
    GL.useNoProgram()
    GL.disable(GL.Blend)
  }

  // Describes the vertices of the bound buffer to the bound vertex array.
  private def describeVertices(paint: PaintProgram): Unit = {
    val stride = paint.floatsPerVertex * 4
    def attribute(location: GL.AttributeLocation, size: Int, offset: Int): Unit = {
      GL.enableVertexAttribArray(location)
      GL.vertexAttribPointer(location, size, GL.FloatType, normalized = false, stride, offset * 4L)
    }
    attribute(paint.position, paint.positionSize, 0)
    attribute(paint.color, 4, paint.positionSize)
    attribute(paint.uv, 2, paint.positionSize + 4)
    attribute(paint.slot, 1, paint.positionSize + 6)
  }

  private def placementOf(scaleX: Float, scaleY: Float, offsetX: Float, offsetY: Float, tint: Color): Array[Float] = Array(
    scaleX, scaleY, offsetX, offsetY,
    tint.red, tint.green, tint.blue, tint.alpha,
    0f, 0f, 0f, 0f,
    0f, 0f, 0f, 0f
  )
  // Shapes are placed on the screen as they are batched; baked ones are not.
  private val placed = placementOf(1f, 1f, 0f, 0f, Color.White)

  private def draw2D(world: World): Unit = {
    val camera = active2DCamera(world)
    val cameraPosition = camera.map(_.position).getOrElse(Vec2(0f, 0f))
    val viewport = camera.map(_.viewportSize).getOrElse(Vec2(app.Window.width.toFloat, app.Window.height.toFloat))

    val screenSize = Vec2(app.Window.width.toFloat, app.Window.height.toFloat)
    val (screenDrawings, worldDrawings) = queue2D.toVector.partition(_.screen)
    GL.disable(GL.DepthTest)
    paintWith(paint2D, vao2D, vbo2D) { batch =>
      GL.uniformMatrix4fv(placementLocation, transpose = false, placed)
      val worldPainter = new Painter(
        batch,
        cameraPosition.x - viewport.x / 2f, cameraPosition.y - viewport.y / 2f, viewport,
        cameraPosition, viewport, screenSize
      )
      val screenPainter = new Painter(batch, 0f, 0f, screenSize, cameraPosition, viewport, screenSize)
      worldDrawings.sortBy(_.key).foreach(_.draw(worldPainter))
      screenDrawings.sortBy(_.key).foreach(_.draw(screenPainter))
    }
    GL.enable(GL.DepthTest)
  }

  private def active2DCamera(world: World): Option[Camera2D] =
    world.index[Camera2D].toVector.filter(_.active).sortBy(-_.priority).headOption

  /** Picks the image file behind a drawable.
    *
    * Atlas-backed drawable variants use their page unless the device cannot
    * hold it, in which case the standalone image is loaded instead.
    */
  private def viewOf(asset: DrawableAsset): ImageView = {
    val variant = asset.bestVariantForDpi(app.Window.logicalPpi)
    variant.atlas match {
      case Some(region) if region.pageWidth <= maxTextureSize && region.pageHeight <= maxTextureSize =>
        val width = region.pageWidth.toFloat
        val height = region.pageHeight.toFloat
        ImageView(
          PageKey(region.pagePath, region.filter, atlasPage = true),
          uOffset = region.x / width,
          vOffset = region.y / height,
          uScale = region.width / width,
          vScale = region.height / height,
          uMin = (region.x + 0.5f) / width,
          vMin = (region.y + 0.5f) / height,
          uMax = (region.x + region.width - 0.5f) / width,
          vMax = (region.y + region.height - 0.5f) / height
        )
      case _ =>
        ImageView(PageKey(variant.resourceName, TextureFilter.Nearest, atlasPage = false))
    }
  }

  /** Loads each page once, however many logical images or concurrent requests use it. */
  private def pageLoader(key: PageKey): Loader[GLTexture] = pages.get(key) match {
    case Some(PageReady(texture)) => Loader.successful(texture)
    case Some(PageFailed(cause)) => Loader.failed(cause)
    case Some(PageLoading(loader)) => loader
    case None => uploadPage(key)
  }

  /** Uploads a page. A page that is already on the GPU keeps its texture until
    * the new one is ready, which then replaces it. The returned loader
    * completes once the page state has been updated.
    */
  private def uploadPage(key: PageKey): Loader[GLTexture] = {
    val generation = pageGenerations.getOrElse(key, 0) + 1
    pageGenerations(key) = generation
    val loader =
      try GL.loadTexture2DResource(key.resourceName, key.filter, generateMipmaps = !key.atlasPage).map(_.texture)
      catch { case NonFatal(cause) => Loader.failed[GLTexture](cause) }
    val updated = new DefaultLoader[GLTexture]
    pages.get(key) match {
      case Some(PageReady(_)) => ()
      case _ => pages(key) = PageLoading(updated.loader)
    }
    loader.onLoad { result =>
      val current = !disposed && pageGenerations.get(key).contains(generation)
      result match {
        case Success(texture) =>
          // An upload can complete after dispose, or after a newer upload of
          // the same page; nothing else owns it then.
          if(!current) GL.deleteTexture(texture)
          else {
            pages.get(key) match {
              case Some(PageReady(previous)) => GL.deleteTexture(previous)
              case _ => ()
            }
            pages(key) = PageReady(texture)
          }
        case Failure(cause) =>
          // A failed reload keeps the image that was there.
          if(current && !pages.get(key).exists(_.isInstanceOf[PageReady])) pages(key) = PageFailed(cause)
      }
      updated.complete(result)
      ()
    }
    updated.loader
  }

  // Runs on the game thread, at the start of a frame.
  private def reloadImages(change: AssetChange): Unit = {
    if(disposed) return
    pages.keys.toVector.foreach { key =>
      if(change.affectsPath(key.resourceName)) change.track(key.resourceName, uploadPage(key))
    }
  }

  private def resolve(texture: Texture2D): Option[ResolvedTexture] =
    viewOf(texture).flatMap { view =>
      pages.get(view.page) match {
        case Some(PageReady(handle)) => Some(ResolvedTexture(handle, view))
        case Some(PageFailed(cause)) => throw cause
        case _ => None
      }
    }

  private type Batch = VertexBatch[GLTexture]

  private def bindUnit(unit: Int, texture: GLTexture): Unit = {
    GL.activeTexture(GL.textureUnit(unit))
    GL.bindTexture(GL.Texture2D, texture)
  }

  // Texture units used by the baked drawings of a batch.
  private var bakedUnits = 0

  private def flush(batch: Batch, vertexArray: GLVertexArray, buffer: GLBuffer): Unit = {
    if(!batch.isEmpty) {
      // Making baked drawings has bound others.
      GL.bindVertexArray(vertexArray)
      GL.bindBuffer(GL.ArrayBuffer, buffer)
      if(batch.length > 0) GL.bufferDataFloat(GL.ArrayBuffer, batch.values, batch.length, GL.StaticDraw)
      bakedUnits = 0
      val used = batch.foreachRun(bindUnit) { (first, count) => GL.drawArrays(GL.Triangles, first, count) }
      var unit = scala.math.max(used, bakedUnits) - 1
      // A bound texture could not be deleted or drawn into freely.
      while(unit >= 0) {
        GL.activeTexture(GL.textureUnit(unit))
        GL.unbindTexture(GL.Texture2D)
        unit -= 1
      }
    }
    batch.clear()
  }

  // Between the draw calls of the 2D batch, whose vertex array is bound again.
  private def drawCompiled(compiled: Compiled, placement: Array[Float]): Unit = {
    GL.uniformMatrix4fv(placementLocation, transpose = false, placement)
    GL.bindVertexArray(compiled.vertexArray)
    compiled.runs.foreach { run =>
      run.textures.indices.foreach(i => bindUnit(i, run.textures(i)))
      bakedUnits = scala.math.max(bakedUnits, run.textures.size)
      GL.drawArrays(GL.Triangles, run.first, run.count)
    }
    GL.bindVertexArray(vao2D)
    GL.uniformMatrix4fv(placementLocation, transpose = false, placed)
  }

  private def findCamera(world: World): Option[Camera3D] = {
    var result: Option[Camera3D] = None
    world.index[Camera3D].foreach { camera =>
      if(camera.active && result.forall(existing => camera.priority > existing.priority)) {
        result = Some(camera)
      }
    }
    result
  }

  private def drawMesh(viewProjection: Mat4, drawing: QueuedMesh): Unit = {
    val QueuedMesh(mesh, materialHandle, model) = drawing
    if(mesh.id < 0 || mesh.id >= meshes.size) return
    if(materialHandle.id < 0 || materialHandle.id >= materials.size) return

    materials(materialHandle.id).foreach { material =>
      meshes(mesh.id) match {
        case ready: MeshReady =>
          // A texture that is still loading, or was released, draws nothing.
          resolve(material.texture).foreach { resolved =>
            val mvp = viewProjection * model
            GL.uniformMatrix4fv(mvpLocation, transpose = false, mvp.values)
            GL.uniformMatrix4fv(modelLocation, transpose = false, model.values)
            GL.uniformMatrix4fv(regionLocation, transpose = false, resolved.view.region)
            GL.uniform1f(emissiveLocation, material.emissive)
            GL.bindTexture(GL.Texture2D, resolved.texture)
            GL.bindVertexArray(ready.vao)
            GL.drawElements(GL.Triangles, ready.indexCount, GL.UnsignedShortType, 0L)
          }
        case MeshLoading | MeshReleased =>
        case MeshFailed(cause) => throw cause
      }
    }
  }

  override def dispose(): Unit = {
    if(disposed) return
    disposed = true
    stopReloading()

    GL.useNoProgram()
    GL.unbindVertexArray()
    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindBuffer(GL.ElementArrayBuffer)
    GL.unbindTexture(GL.Texture2D)

    meshes.foreach {
      case mesh: MeshReady =>
        GL.deleteBuffer(mesh.indexBuffer)
        GL.deleteBuffer(mesh.vertexBuffer)
        GL.deleteVertexArray(mesh.vao)
      case MeshLoading =>
      case MeshReleased =>
      case MeshFailed(_) =>
    }
    meshes.clear()

    textures.clear()
    materials.clear()
    pages.values.foreach {
      case PageReady(texture) => GL.deleteTexture(texture)
      case _ => ()
    }
    pages.clear()
    pageUses.clear()
    fontTextures.values.foreach(GL.deleteTexture)
    fontTextures.clear()

    bakedDrawings.foreach(deleteCompiled)
    bakedDrawings.clear()

    GL.deleteBuffer(vbo2D)
    GL.deleteVertexArray(vao2D)
    buffers3D.foreach { case (vertexArray, buffer) =>
      GL.deleteBuffer(buffer)
      GL.deleteVertexArray(vertexArray)
    }
    GL.deleteProgram(paint3D.program)
    GL.deleteProgram(paint2D.program)
    GL.deleteProgram(program)
  }

  private def ensureActive(): Unit = {
    if(disposed) throw new IllegalStateException("Renderer has been disposed")
  }

  private def createProgram(): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + """

      in vec3 a_position;
      in vec3 a_normal;
      in vec2 a_uv;

      uniform mat4 u_mvp;
      uniform mat4 u_model;

      out vec3 v_normal;
      out vec2 v_uv;

      void main() {
        gl_Position = u_mvp * vec4(a_position, 1.0);
        v_normal = normalize(transpose(inverse(mat3(u_model))) * a_normal);
        v_uv = a_uv;
      }
    """

    val fragmentShaderSource = GL.ShaderHeader + """

      in vec3 v_normal;
      in vec2 v_uv;

      uniform sampler2D u_texture;
      uniform float u_emissive;
      // Where the image is in the texture: offset and scale, then the bounds
      // its coordinates are clamped to.
      uniform mat4 u_region;

      out vec4 out_color;

      void main() {
        vec3 light_dir = normalize(vec3(-0.5, 0.4, 1.0));
        float diffuse = max(dot(normalize(v_normal), light_dir), 0.0);
        float light = max(u_emissive, 0.18 + diffuse * 0.9);
        vec2 uv = clamp(u_region[0].xy + v_uv * u_region[0].zw, u_region[1].xy, u_region[1].zw);
        vec3 color = texture(u_texture, uv).rgb * light;
        out_color = vec4(color, 1.0);
      }
    """

    val vertexShader = compileShader(GL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(GL.FragmentShader, fragmentShaderSource)

    val shaderProgram = GL.createProgram()
    GL.attachShader(shaderProgram, vertexShader)
    GL.attachShader(shaderProgram, fragmentShader)
    GL.linkProgram(shaderProgram)
    if(GL.getProgramParameter(shaderProgram, GL.LinkStatus) == 0) {
      throw new RuntimeException("Program link failed: " + GL.getProgramInfoLog(shaderProgram))
    }
    GL.deleteShader(vertexShader)
    GL.deleteShader(fragmentShader)
    shaderProgram
  }

  // Colored or textured shapes, in 2D or 3D depending on their positions.
  private def createPaintProgram(name: String, positionType: String, uniforms: String, projected: String, color: String): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + s"""

      in $positionType a_position;
      in vec4 a_color;
      in vec2 a_uv;
      in float a_slot;
      $uniforms
      out vec4 v_color;
      out vec2 v_uv;
      flat out float v_slot;

      void main() {
        gl_Position = $projected;
        v_color = $color;
        v_uv = a_uv;
        v_slot = a_slot;
      }
    """

    val fragmentShaderSource = VertexBatch.fragmentShader(GL.ShaderHeader, textureSlots, additive = true)

    val vertexShader = compileShader(GL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(GL.FragmentShader, fragmentShaderSource)
    val shaderProgram = GL.createProgram()
    GL.attachShader(shaderProgram, vertexShader)
    GL.attachShader(shaderProgram, fragmentShader)
    GL.linkProgram(shaderProgram)
    if(GL.getProgramParameter(shaderProgram, GL.LinkStatus) == 0) {
      throw new RuntimeException(name + " program link failed: " + GL.getProgramInfoLog(shaderProgram))
    }
    GL.deleteShader(vertexShader)
    GL.deleteShader(fragmentShader)
    shaderProgram
  }

  private def compileShader(shaderType: GL.ShaderType, source: String): GL.Shader = {
    val shader = GL.createShader(shaderType)
    GL.shaderSource(shader, source)
    GL.compileShader(shader)
    if(GL.getShaderParameter(shader, GL.CompileStatus) == 0) {
      throw new RuntimeException("Shader compilation failed: " + GL.getShaderInfoLog(shader))
    }
    shader
  }

  private def requireAttribute(program: GLProgram, name: String): GL.AttributeLocation =
    GL.getAttribLocation(program, name).getOrElse(throw new RuntimeException("Missing attribute: " + name))

  private def requireUniform(program: GLProgram, name: String): GLUniformLocation =
    GL.getUniformLocation(program, name).getOrElse(throw new RuntimeException("Missing uniform: " + name))
}
