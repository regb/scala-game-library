package sgl.engine.gameobject.opengl

import scala.collection.mutable
import scala.util.{Failure, Success}

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{CompositionError, Scope, World}
import sgl.engine.gameobject.runtime.ClearColor
import sgl.engine.gameobject.spatial2d.Spatial2D
import sgl.assets.{DrawableAsset, RawImageAsset, TextureFilter}
import sgl.engine.gameobject.render2d.{Camera2D, Painter2D, Render2D, Texture2D, TextureSource}
import sgl.engine.gameobject.render3d._
import sgl.math.{Mat4, Vec2}
import sgl.util.Loader

final class OpenGLRenderer(
    private val app: OpenGLProvider with SystemProvider with WindowProvider
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
  private sealed trait TextureState
  private final case class TextureReady(texture: GLTexture) extends TextureState
  private final case class TextureFailed(cause: Throwable) extends TextureState
  private final case class TextureLoading(loader: Option[Loader[Texture2D]] = None) extends TextureState
  private case object TextureReleased extends TextureState
  private final case class GLMaterial(texture: Texture2D, emissive: Float)

  /** One uploaded image: a standalone drawable or raw image, or a texture-atlas page. */
  private final case class PageKey(resourceName: String, filter: TextureFilter, atlasPage: Boolean)
  private sealed trait PageState
  private final case class PageLoading(loader: Loader[GLTexture]) extends PageState
  private final case class PageReady(texture: GLTexture) extends PageState
  private final case class PageFailed(cause: Throwable) extends PageState

  /** Where a logical 2D image lives inside its page: `pageU = uOffset + u * uScale`.
    *
    * Standalone images use the identity transform. Sprite and tile UVs stay
    * relative to the logical image, so crops and flips compose unchanged.
    */
  private final case class ImageView(page: PageKey, uOffset: Float, vOffset: Float, uScale: Float, vScale: Float) {
    def u(local: Float): Float = uOffset + local * uScale
    def v(local: Float): Float = vOffset + local * vScale
  }
  private final case class ResolvedTexture(texture: GLTexture, view: ImageView)

  private val program: GLProgram = createProgram()
  private val program2D: GLProgram = create2DProgram()
  private val mvpLocation: GLUniformLocation = requireUniform(program, "u_mvp")
  private val modelLocation: GLUniformLocation = requireUniform(program, "u_model")
  private val textureLocation: GLUniformLocation = requireUniform(program, "u_texture")
  private val emissiveLocation: GLUniformLocation = requireUniform(program, "u_emissive")
  private val vao2D: GLVertexArray = GL.genVertexArray()
  private val vbo2D: GLBuffer = GL.genBuffer()
  private val position2DLocation = requireAttribute(program2D, "a_position")
  private val color2DLocation = requireAttribute(program2D, "a_color")
  private val uv2DLocation = requireAttribute(program2D, "a_uv")
  private val texture2DLocation = requireUniform(program2D, "u_texture")
  private val useTexture2DLocation = requireUniform(program2D, "u_useTexture")

  private val meshes = mutable.ArrayBuffer.empty[MeshState]
  private val textures = mutable.ArrayBuffer.empty[TextureState]
  // Physical pages own the GPU textures and are deleted only on dispose. Logical
  // image views never own a texture, so several drawables can share one page.
  private val pages = mutable.HashMap.empty[PageKey, PageState]
  private val imageViews = mutable.HashMap.empty[TextureSource, ImageView]
  private val maxTextureSize: Int = GL.maxTextureSize
  private val materials = mutable.ArrayBuffer.empty[Option[GLMaterial]]
  private var disposed = false

  GL.enable(GL.DepthTest)

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

  override def loadTexture2D(asset: DrawableAsset)(using Scope): Texture2D = loadStandalone(GL.loadTexture2D(asset))
  override def loadTexture2D(asset: RawImageAsset)(using Scope): Texture2D = loadStandalone(GL.loadTexture2D(asset))

  private def loadStandalone(loader: => Loader[GLTexture])(using scope: Scope): Texture2D = owned(scope) {
    val id = textures.size
    textures += TextureLoading()
    loader.onLoad {
      case Success(texture) =>
        if(disposed || textures(id) == TextureReleased) GL.deleteTexture(texture)
        else textures(id) = TextureReady(texture)
      case Failure(cause) => if(textures(id) != TextureReleased) textures(id) = TextureFailed(cause)
    }
    new Texture2D(TextureSource.Standalone(id))
  }(releaseTexture)

  override def createMaterial(texture: Texture2D, emissive: Float)(using scope: Scope): Material = {
    val partOfPage = imageViews.get(texture.source).exists(view => view.uScale != 1f || view.vScale != 1f || view.uOffset != 0f || view.vOffset != 0f)
    if(partOfPage) {
      throw new CompositionError(
        "A material needs an image of its own, and this texture is a region of an atlas page. " +
          "Load the image with renderer.loadTexture2D instead"
      )
    }
    owned(scope) {
      val id = materials.size
      materials += Some(GLMaterial(texture, emissive))
      new Material(id)
    }(releaseMaterial)
  }

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

  // Only an image uploaded on its own is released; atlas pages are shared by
  // every texture cut from them and stay for the life of the renderer.
  override def releaseTexture(texture: Texture2D): Unit = texture.source match {
    case TextureSource.Standalone(id) if id >= 0 && id < textures.size =>
      textures(id) match {
        case TextureReady(handle) => GL.deleteTexture(handle)
        case _ => ()
      }
      textures(id) = TextureReleased
    case _ => ()
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

  // Turns points of one space, world or screen, into batched vertices.
  private final class Painter(
      batch: Batch2D,
      inWorld: Boolean,
      override val camera: Vec2,
      override val viewport: Vec2,
      override val screenSize: Vec2
  ) extends Painter2D {
    private def put(out: Array[Float], index: Int, p: Vec2, color: (Float, Float, Float, Float), u: Float, v: Float): Unit = {
      val offset = index * 8
      if(inWorld) {
        out(offset) = ((p.x - camera.x) / viewport.x) * 2f
        out(offset + 1) = -((p.y - camera.y) / viewport.y) * 2f
      } else {
        out(offset) = (p.x / screenSize.x) * 2f - 1f
        out(offset + 1) = 1f - (p.y / screenSize.y) * 2f
      }
      out(offset + 2) = color._1
      out(offset + 3) = color._2
      out(offset + 4) = color._3
      out(offset + 5) = color._4
      out(offset + 6) = u
      out(offset + 7) = v
    }

    override def triangle(a: Vec2, b: Vec2, c: Vec2, color: (Float, Float, Float, Float)): Unit = {
      val out = new Array[Float](3 * 8)
      put(out, 0, a, color, 0f, 0f)
      put(out, 1, b, color, 0f, 0f)
      put(out, 2, c, color, 0f, 0f)
      batch.add(None, out)
    }

    override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: (Float, Float, Float, Float)): Unit =
      textured(None, a, b, c, d, 0f, 0f, 0f, 0f, color)

    override def image(
        texture: Texture2D,
        a: Vec2, b: Vec2, c: Vec2, d: Vec2,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: (Float, Float, Float, Float)
    ): Unit =
      textureForSource(texture.source) match {
        case Some(resolved) =>
          val view = resolved.view
          textured(Some(resolved.texture), a, b, c, d, view.u(u0), view.v(v0), view.u(u1), view.v(v1), color)
        case None =>
          textured(None, a, b, c, d, u0, v0, u1, v1, color)
      }

    private def textured(
        texture: Option[GLTexture],
        a: Vec2, b: Vec2, c: Vec2, d: Vec2,
        u0: Float, v0: Float, u1: Float, v1: Float,
        color: (Float, Float, Float, Float)
    ): Unit = {
      val out = new Array[Float](6 * 8)
      put(out, 0, a, color, u0, v0)
      put(out, 1, b, color, u1, v0)
      put(out, 2, c, color, u1, v1)
      put(out, 3, a, color, u0, v0)
      put(out, 4, c, color, u1, v1)
      put(out, 5, d, color, u0, v1)
      batch.add(texture, out)
    }
  }

  override def beginFrame(world: World): Unit = {
    queue2D.clear()
    if(disposed) return

    val clear = world.globals.getOption[ClearColor]
      .map(c => (c.red, c.green, c.blue, c.alpha))
      .getOrElse((0f, 0f, 0f, 1f))
    GL.clearColor(clear._1, clear._2, clear._3, clear._4)
    GL.clear(GL.clearMask(GL.ColorBufferBit, GL.DepthBufferBit))
  }

  override private[gameobject] def drawMeshes(world: World): Unit = {
    if(disposed) return

    findCamera(world).foreach { camera =>
      val aspect = app.Window.width.toFloat / scala.math.max(1, app.Window.height).toFloat
      val projection = Mat4.perspective(camera.fovRadians, aspect, camera.near, camera.far)
      val viewProjection = projection * camera.viewMatrix

      GL.useProgram(program)
      GL.activeTexture(GL.Texture0)
      GL.uniform1i(textureLocation, 0)

      world.index[MeshRenderer].foreach { meshRenderer =>
        drawRenderable(viewProjection, meshRenderer.spatial.globalMatrix, meshRenderer)
      }

      GL.unbindVertexArray()
      GL.unbindTexture(GL.Texture2D)
      GL.useNoProgram()
    }
  }

  override def endFrame(world: World): Unit = {
    if(disposed || queue2D.isEmpty) {
      queue2D.clear()
      return
    }

    GL.disable(GL.DepthTest)
    GL.enable(GL.Blend)
    GL.blendFunc(GL.SrcAlpha, GL.OneMinusSrcAlpha)
    GL.useProgram(program2D)
    GL.bindVertexArray(vao2D)
    GL.bindBuffer(GL.ArrayBuffer, vbo2D)
    GL.enableVertexAttribArray(position2DLocation)
    GL.vertexAttribPointer(position2DLocation, 2, GL.FloatType, normalized = false, 8 * 4, 0L)
    GL.enableVertexAttribArray(color2DLocation)
    GL.vertexAttribPointer(color2DLocation, 4, GL.FloatType, normalized = false, 8 * 4, 2L * 4L)
    GL.enableVertexAttribArray(uv2DLocation)
    GL.vertexAttribPointer(uv2DLocation, 2, GL.FloatType, normalized = false, 8 * 4, 6L * 4L)
    GL.activeTexture(GL.Texture0)
    GL.uniform1i(texture2DLocation, 0)

    val camera = active2DCamera(world)
    val cameraPosition = camera.flatMap(_.owner.component[Spatial2D]).map(_.global.position).getOrElse(Vec2(0f, 0f))
    val viewport = camera.map(_.viewportSize).getOrElse(Vec2(app.Window.width.toFloat, app.Window.height.toFloat))

    val screenSize = Vec2(app.Window.width.toFloat, app.Window.height.toFloat)
    val batch = new Batch2D
    val worldPainter = new Painter(batch, inWorld = true, cameraPosition, viewport, screenSize)
    val screenPainter = new Painter(batch, inWorld = false, cameraPosition, viewport, screenSize)
    val (screenDrawings, worldDrawings) = queue2D.toVector.partition(_.screen)
    queue2D.clear()
    worldDrawings.sortBy(_.key).foreach(_.draw(worldPainter))
    screenDrawings.sortBy(_.key).foreach(_.draw(screenPainter))
    batch.flush()

    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindVertexArray()
    GL.useNoProgram()
    GL.disable(GL.Blend)
    GL.enable(GL.DepthTest)
  }

  private def active2DCamera(world: World): Option[Camera2D] =
    world.index[Camera2D].toVector.filter(_.active).sortBy(-_.priority).headOption

  def loadDrawableTexture(asset: DrawableAsset): Loader[Texture2D] =
    loadTexture(TextureSource.Drawable(asset), Texture2D.loaded(asset))

  def loadRawImageTexture(asset: RawImageAsset): Loader[Texture2D] =
    loadTexture(TextureSource.RawImage(asset), Texture2D.loaded(asset))

  private def loadTexture(source: TextureSource, handle: Texture2D): Loader[Texture2D] = {
    ensureActive()
    val view = imageViews.getOrElseUpdate(source, resolveView(source))
    pageLoader(view.page).map(_ => handle)
  }

  /** Picks the physical image behind a logical texture.
    *
    * Atlas-backed drawable variants use their page unless the device cannot
    * hold it, in which case the standalone image is loaded instead.
    */
  private def resolveView(source: TextureSource): ImageView = source match {
    case TextureSource.Standalone(_) => wholeImage
    case TextureSource.RawImage(asset) =>
      ImageView(PageKey(asset.resourceName, TextureFilter.Nearest, atlasPage = false), 0f, 0f, 1f, 1f)
    case TextureSource.Drawable(asset) =>
      val variant = asset.bestVariantForDpi(app.Window.logicalPpi)
      variant.atlas match {
        case Some(region) if region.pageWidth <= maxTextureSize && region.pageHeight <= maxTextureSize =>
          ImageView(
            PageKey(region.pagePath, region.filter, atlasPage = true),
            uOffset = region.x.toFloat / region.pageWidth,
            vOffset = region.y.toFloat / region.pageHeight,
            uScale = region.width.toFloat / region.pageWidth,
            vScale = region.height.toFloat / region.pageHeight
          )
        case _ =>
          ImageView(PageKey(variant.resourceName, TextureFilter.Nearest, atlasPage = false), 0f, 0f, 1f, 1f)
      }
  }

  /** Loads each page once, however many logical images or concurrent requests use it. */
  private def pageLoader(key: PageKey): Loader[GLTexture] = pages.get(key) match {
    case Some(PageReady(texture)) => Loader.successful(texture)
    case Some(PageFailed(cause)) => Loader.failed(cause)
    case Some(PageLoading(loader)) => loader
    case None =>
      val loader = GL.loadTexture2DResource(key.resourceName, key.filter, generateMipmaps = !key.atlasPage).map(_.texture)
      pages(key) = PageLoading(loader)
      loader.onLoad {
        case Success(texture) =>
          // An upload can complete after dispose; nothing else owns it then.
          if(disposed) GL.deleteTexture(texture)
          else pages(key) = PageReady(texture)
        case Failure(cause) => if(!disposed) pages(key) = PageFailed(cause)
      }
      loader
  }

  private val wholeImage = ImageView(PageKey("", TextureFilter.Nearest, atlasPage = false), 0f, 0f, 1f, 1f)

  private def textureForSource(source: TextureSource): Option[ResolvedTexture] = source match {
    case TextureSource.Standalone(id) =>
      if(id < 0 || id >= textures.size) None
      else textures(id) match {
        case TextureReady(texture) => Some(ResolvedTexture(texture, wholeImage))
        case TextureFailed(cause) => throw cause
        case _ => None
      }
    case _ => pageTextureForSource(source)
  }

  private def pageTextureForSource(source: TextureSource): Option[ResolvedTexture] =
    imageViews.get(source).flatMap { view =>
      pages.get(view.page) match {
        case Some(PageReady(texture)) => Some(ResolvedTexture(texture, view))
        case Some(PageFailed(cause)) => throw cause
        case _ => None
      }
    }

  /** Accumulates consecutive 2D draws that share a texture into one draw call.
    *
    * Draws are never reordered, so z ordering and blending are unchanged: a
    * batch is flushed whenever the texture changes or it reaches capacity.
    */
  private final class Batch2D {
    private val MaxFloats = 6 * 8 * 1024
    private var vertices = new Array[Float](MaxFloats)
    private var size = 0
    private var texture: Option[GLTexture] = None

    def add(nextTexture: Option[GLTexture], data: Array[Float]): Unit = {
      if(data.isEmpty) return
      if(size > 0 && (nextTexture != texture || size + data.length > MaxFloats)) flush()
      texture = nextTexture
      if(size + data.length > vertices.length) vertices = java.util.Arrays.copyOf(vertices, size + data.length)
      System.arraycopy(data, 0, vertices, size, data.length)
      size += data.length
    }

    def flush(): Unit = {
      if(size == 0) return
      texture match {
        case Some(t) =>
          GL.uniform1f(useTexture2DLocation, 1f)
          GL.bindTexture(GL.Texture2D, t)
        case None =>
          GL.uniform1f(useTexture2DLocation, 0f)
          GL.unbindTexture(GL.Texture2D)
      }
      GL.bufferDataFloat(GL.ArrayBuffer, java.util.Arrays.copyOf(vertices, size), GL.StaticDraw)
      GL.drawArrays(GL.Triangles, 0, size / 8)
      size = 0
    }
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

  private def drawRenderable(viewProjection: Mat4, model: Mat4, meshRenderer: MeshRenderer): Unit = {
    if(meshRenderer.mesh.id < 0 || meshRenderer.mesh.id >= meshes.size) return
    if(meshRenderer.material.id < 0 || meshRenderer.material.id >= materials.size) return

    if(materials(meshRenderer.material.id).isEmpty) return
    val material = materials(meshRenderer.material.id).get

    meshes(meshRenderer.mesh.id) match {
      case mesh: MeshReady =>
        // A texture that is still loading, or was released, draws nothing.
        textureForSource(material.texture.source).foreach { resolved =>
          val mvp = viewProjection * model
          GL.uniformMatrix4fv(mvpLocation, transpose = false, mvp.values)
          GL.uniformMatrix4fv(modelLocation, transpose = false, model.values)
          GL.uniform1f(emissiveLocation, material.emissive)
          GL.bindTexture(GL.Texture2D, resolved.texture)
          GL.bindVertexArray(mesh.vao)
          GL.drawElements(GL.Triangles, mesh.indexCount, GL.UnsignedShortType, 0L)
        }
      case MeshLoading | MeshReleased =>
      case MeshFailed(cause) => throw cause
    }
  }

  override def dispose(): Unit = {
    if(disposed) return
    disposed = true

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

    textures.foreach {
      case TextureReady(texture) => GL.deleteTexture(texture)
      case TextureLoading(_) =>
      case TextureReleased =>
      case TextureFailed(_) =>
    }
    textures.clear()
    materials.clear()
    pages.values.foreach {
      case PageReady(texture) => GL.deleteTexture(texture)
      case _ => ()
    }
    pages.clear()
    imageViews.clear()

    GL.deleteBuffer(vbo2D)
    GL.deleteVertexArray(vao2D)
    GL.deleteProgram(program2D)
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

      out vec4 out_color;

      void main() {
        vec3 light_dir = normalize(vec3(-0.5, 0.4, 1.0));
        float diffuse = max(dot(normalize(v_normal), light_dir), 0.0);
        float light = max(u_emissive, 0.18 + diffuse * 0.9);
        vec3 color = texture(u_texture, v_uv).rgb * light;
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

  private def create2DProgram(): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + """

      in vec2 a_position;
      in vec4 a_color;
      in vec2 a_uv;
      out vec4 v_color;
      out vec2 v_uv;

      void main() {
        gl_Position = vec4(a_position, 0.0, 1.0);
        v_color = a_color;
        v_uv = a_uv;
      }
    """

    val fragmentShaderSource = GL.ShaderHeader + """

      in vec4 v_color;
      in vec2 v_uv;
      uniform sampler2D u_texture;
      uniform float u_useTexture;
      out vec4 out_color;

      void main() {
        vec4 tex = texture(u_texture, v_uv);
        out_color = mix(v_color, tex * v_color, u_useTexture);
      }
    """

    val vertexShader = compileShader(GL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(GL.FragmentShader, fragmentShaderSource)
    val shaderProgram = GL.createProgram()
    GL.attachShader(shaderProgram, vertexShader)
    GL.attachShader(shaderProgram, fragmentShader)
    GL.linkProgram(shaderProgram)
    if(GL.getProgramParameter(shaderProgram, GL.LinkStatus) == 0) {
      throw new RuntimeException("2D program link failed: " + GL.getProgramInfoLog(shaderProgram))
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
