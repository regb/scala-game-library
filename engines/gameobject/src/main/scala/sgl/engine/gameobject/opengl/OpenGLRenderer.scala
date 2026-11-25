package sgl.engine.gameobject.opengl

import scala.collection.mutable
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

import sgl.{ClipRect, OpenGLProvider, PortableFont, SystemProvider, VertexBatch, WindowProvider}
import sgl.engine.gameobject.{ClearColor, Color, CompositionError, Scope, World}
import sgl.assets.{AssetChange, AssetChangeListener, AssetReloads, DrawableAsset, RawImageAsset, TextureFilter}
import sgl.engine.gameobject.render2d.{Camera2D, Painter2D, Render2D, Texture2D}
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
  private val paint2D = new PaintProgram(createPaintProgram("2D", "vec2", "vec4(a_position, 0.0, 1.0)"), positionSize = 2)
  private val paint3D = new PaintProgram(createPaintProgram("3D", "vec3", "u_viewProjection * vec4(a_position, 1.0)"), positionSize = 3)
  private val mvpLocation: GLUniformLocation = requireUniform(program, "u_mvp")
  private val modelLocation: GLUniformLocation = requireUniform(program, "u_model")
  private val textureLocation: GLUniformLocation = requireUniform(program, "u_texture")
  private val emissiveLocation: GLUniformLocation = requireUniform(program, "u_emissive")
  private val regionLocation: GLUniformLocation = requireUniform(program, "u_region")
  private val viewProjection3DLocation: GLUniformLocation = requireUniform(paint3D.program, "u_viewProjection")
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

  // Turns points of one space, world or screen, into batched vertices.
  private final class Painter(
      batch: Batch,
      inWorld: Boolean,
      override val camera: Vec2,
      override val viewport: Vec2,
      override val screenSize: Vec2
  ) extends Painter2D {
    // The edges of the screen.
    private val left = if(inWorld) camera.x - viewport.x / 2f else 0f
    private val right = if(inWorld) camera.x + viewport.x / 2f else screenSize.x
    private val top = if(inWorld) camera.y - viewport.y / 2f else 0f
    private val bottom = if(inWorld) camera.y + viewport.y / 2f else screenSize.y
    private var currentClip: Option[ClipRect] = None

    // Maps are often much larger than what is shown of them.
    private def offScreen(a: Vec2, b: Vec2, c: Vec2, d: Vec2): Boolean =
      (a.x < left && b.x < left && c.x < left && d.x < left) ||
        (a.x > right && b.x > right && c.x > right && d.x > right) ||
        (a.y < top && b.y < top && c.y < top && d.y < top) ||
        (a.y > bottom && b.y > bottom && c.y > bottom && d.y > bottom)

    private def put(p: Vec2, color: Color, u: Float, v: Float): Unit = {
      val out = batch.values
      val offset = batch.next()
      if(inWorld) {
        out(offset) = ((p.x - camera.x) / viewport.x) * 2f
        out(offset + 1) = -((p.y - camera.y) / viewport.y) * 2f
      } else {
        out(offset) = (p.x / screenSize.x) * 2f - 1f
        out(offset + 1) = 1f - (p.y / screenSize.y) * 2f
      }
      out(offset + 2) = color.red
      out(offset + 3) = color.green
      out(offset + 4) = color.blue
      out(offset + 5) = color.alpha
      out(offset + 6) = u
      out(offset + 7) = v
      out(offset + 8) = batch.slot
    }

    override def triangle(a: Vec2, b: Vec2, c: Vec2, color: Color): Unit = {
      batch.begin(None, 3)
      put(a, color, 0f, 0f)
      put(b, color, 0f, 0f)
      put(c, color, 0f, 0f)
    }

    override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit =
      textured(None, a, b, c, d, 0f, 0f, 0f, 0f, color)

    override def image(
        texture: Texture2D,
        a: Vec2, b: Vec2, c: Vec2, d: Vec2,
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

    override def clip(position: Vec2, size: Vec2)(body: => Unit): Unit = {
      def pixelX(x: Float): Float = if(inWorld) (x - left) / viewport.x * screenSize.x else x
      def pixelY(y: Float): Float = if(inWorld) (y - top) / viewport.y * screenSize.y else y
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
      flush(batch)
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
          val x1 = x0 + glyph.width * scale
          val y1 = y0 + glyph.height * scale
          textured(
            texture,
            Vec2(x0, y0), Vec2(x1, y0), Vec2(x1, y1), Vec2(x0, y1),
            glyph.x.toFloat / face.atlasWidth, glyph.y.toFloat / face.atlasHeight,
            (glyph.x + glyph.width).toFloat / face.atlasWidth, (glyph.y + glyph.height).toFloat / face.atlasHeight,
            color
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
      put(a, color, u0, v0)
      put(b, color, u1, v0)
      put(c, color, u1, v1)
      put(a, color, u0, v0)
      put(c, color, u1, v1)
      put(d, color, u0, v1)
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
    val stride = paint.floatsPerVertex * 4
    GL.enable(GL.Blend)
    GL.blendSourceOver()
    GL.useProgram(paint.program)
    GL.bindVertexArray(vertexArray)
    GL.bindBuffer(GL.ArrayBuffer, buffer)
    def attribute(location: GL.AttributeLocation, size: Int, offset: Int): Unit = {
      GL.enableVertexAttribArray(location)
      GL.vertexAttribPointer(location, size, GL.FloatType, normalized = false, stride, offset * 4L)
    }
    attribute(paint.position, paint.positionSize, 0)
    attribute(paint.color, 4, paint.positionSize)
    attribute(paint.uv, 2, paint.positionSize + 4)
    attribute(paint.slot, 1, paint.positionSize + 6)

    paint.batch.clear()
    draw(paint.batch)
    flush(paint.batch)

    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindVertexArray()
    GL.useNoProgram()
    GL.disable(GL.Blend)
  }

  private def draw2D(world: World): Unit = {
    val camera = active2DCamera(world)
    val cameraPosition = camera.map(_.position).getOrElse(Vec2(0f, 0f))
    val viewport = camera.map(_.viewportSize).getOrElse(Vec2(app.Window.width.toFloat, app.Window.height.toFloat))

    val screenSize = Vec2(app.Window.width.toFloat, app.Window.height.toFloat)
    val (screenDrawings, worldDrawings) = queue2D.toVector.partition(_.screen)
    GL.disable(GL.DepthTest)
    paintWith(paint2D, vao2D, vbo2D) { batch =>
      val worldPainter = new Painter(batch, inWorld = true, cameraPosition, viewport, screenSize)
      val screenPainter = new Painter(batch, inWorld = false, cameraPosition, viewport, screenSize)
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

  private def flush(batch: Batch): Unit = {
    if(!batch.isEmpty) {
      GL.bufferDataFloat(GL.ArrayBuffer, batch.values, batch.length, GL.StaticDraw)
      var unit = batch.foreachRun { (unit, texture) =>
        GL.activeTexture(GL.textureUnit(unit))
        GL.bindTexture(GL.Texture2D, texture)
      } { (first, count) => GL.drawArrays(GL.Triangles, first, count) } - 1
      // A bound texture could not be deleted or drawn into freely.
      while(unit >= 0) {
        GL.activeTexture(GL.textureUnit(unit))
        GL.unbindTexture(GL.Texture2D)
        unit -= 1
      }
    }
    batch.clear()
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
  private def createPaintProgram(name: String, positionType: String, projected: String): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + s"""

      in $positionType a_position;
      in vec4 a_color;
      in vec2 a_uv;
      in float a_slot;
      uniform mat4 u_viewProjection;
      out vec4 v_color;
      out vec2 v_uv;
      flat out float v_slot;

      void main() {
        gl_Position = $projected;
        v_color = a_color;
        v_uv = a_uv;
        v_slot = a_slot;
      }
    """

    val fragmentShaderSource = VertexBatch.fragmentShader(GL.ShaderHeader, textureSlots)

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
