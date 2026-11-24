package sgl.engine.gameobject

import scala.collection.mutable

import org.scalatest.funsuite.AnyFunSuite

import sgl.{OpenGLProvider, PartsResourcePathProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetChange, AssetFactory, AssetReloadHub, BinaryAsset, DrawableAsset, TextAsset, TextureFilter}
import sgl.engine.gameobject.opengl.OpenGLRenderer
import sgl.engine.gameobject.spatial2d._
import sgl.engine.gameobject.render2d.{Render2DModule, SpriteRenderSystem, SpriteRenderer, SpriteVisual, Texture2D, TileQuad, Tilemap, TilemapRenderSystem}
import sgl.engine.gameobject.render3d.{Camera3D, MeshData, Render3DModule}
import sgl.engine.gameobject.spatial3d._
import sgl.math.{Mat4, Vec2, Vec3}
import sgl.util.{DefaultLoader, Loader}

class OpenGLRendererSuite extends AnyFunSuite {

  /** Records the calls that matter for texture sharing and batching. */
  final class FakeGLApp(maxTexture: Int = 2048) extends OpenGLProvider with SystemProvider with PartsResourcePathProvider with WindowProvider {
    final case class Upload(resourceName: String, filter: TextureFilter, mipmaps: Boolean, loader: DefaultLoader[Int], texture: Int) {
      def complete(): Unit = loader.success(texture)
    }
    val uploads = mutable.ArrayBuffer.empty[Upload]
    val deleted = mutable.ArrayBuffer.empty[Int]
    /** One entry per draw call: the bound texture (0 when none) and the uploaded vertices. */
    val draws = mutable.ArrayBuffer.empty[(Int, Array[Float])]
    private var nextTexture = 100
    private var bound = 0
    private var buffer = Array.empty[Float]
    private var stride = 0
    var depthWrites = true
    /** One entry per mesh drawn: the bound texture and the matrices set for it. */
    val meshDraws = mutable.ArrayBuffer.empty[(Int, Vector[Vector[Float]])]
    private var matrices = Vector.empty[Vector[Float]]

    override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
    override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
    override val System: System = new System {
      override def exit(): Unit = ()
      override def currentTimeMillis: Long = 0L
      override def nanoTime: Long = 0L
      override def loadText(asset: TextAsset): Loader[Array[String]] = Loader.failed(new NoSuchElementException)
      override def loadBinary(asset: BinaryAsset): Loader[Array[Byte]] = Loader.failed(new NoSuchElementException)
      override def openWebpage(uri: java.net.URI): Unit = ()
    }

    final class FakeWindow extends AbstractWindow {
      override def width: Int = 200
      override def height: Int = 100
      override def xppi: Float = 160f
      override def yppi: Float = 160f
      override def logicalPpi: Float = 160f
    }
    override type Window = FakeWindow
    override val Window: Window = new FakeWindow

    object FakeGL extends OpenGL {
      type Shader = Int; type Program = Int; type Buffer = Int; type VertexArray = Int; type Texture = Int
      type UniformLocation = Int; type AttributeLocation = Int
      type ShaderType = Int; type ShaderParameter = Int; type ProgramParameter = Int; type Capability = Int
      type Face = Int; type BlendFactor = Int; type BufferTarget = Int; type BufferUsage = Int
      type TextureTarget = Int; type TextureUnit = Int; type TextureParameter = Int; type TextureParameterValue = Int
      type DataType = Int; type DrawMode = Int; type ClearMask = Int

      override val ShaderHeader: String = ""
      override val ColorBufferBit, DepthBufferBit, FloatType, UnsignedShortType, Triangles = 0
      override val DepthTest, ScissorTest, CullFace, Back, Blend, SrcAlpha, OneMinusSrcAlpha = 0
      override val VertexShader, FragmentShader, CompileStatus, LinkStatus = 0
      override val ArrayBuffer, ElementArrayBuffer, StaticDraw = 0
      override val Texture2D, Texture0, TextureMinFilter, TextureMagFilter, TextureWrapS, TextureWrapT = 0
      override val Linear, Nearest, ClampToEdge = 0

      override def clearMask(mask: Int, masks: Int*): Int = mask
      override def viewport(x: Int, y: Int, width: Int, height: Int): Unit = ()
      override def enable(capability: Int): Unit = ()
      override def disable(capability: Int): Unit = ()
      override def scissor(x: Int, y: Int, width: Int, height: Int): Unit = ()
      override def cullFace(mode: Int): Unit = ()
      override def blendFunc(source: Int, destination: Int): Unit = ()
      override def depthMask(write: Boolean): Unit = depthWrites = write
      override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = ()
      override def clear(mask: Int): Unit = ()
      override def createShader(shaderType: Int): Int = 1
      override def shaderSource(shader: Int, source: String): Unit = ()
      override def compileShader(shader: Int): Unit = ()
      override def getShaderParameter(shader: Int, parameter: Int): Int = 1
      override def getShaderInfoLog(shader: Int): String = ""
      override def deleteShader(shader: Int): Unit = ()
      override def createProgram(): Int = 1
      override def attachShader(program: Int, shader: Int): Unit = ()
      override def linkProgram(program: Int): Unit = ()
      override def getProgramParameter(program: Int, parameter: Int): Int = 1
      override def getProgramInfoLog(program: Int): String = ""
      override def useProgram(program: Int): Unit = ()
      override def useNoProgram(): Unit = ()
      override def deleteProgram(program: Int): Unit = ()
      override def genVertexArray(): Int = 1
      override def bindVertexArray(vertexArray: Int): Unit = ()
      override def unbindVertexArray(): Unit = ()
      override def deleteVertexArray(vertexArray: Int): Unit = ()
      override def genBuffer(): Int = 1
      override def bindBuffer(target: Int, buffer: Int): Unit = ()
      override def unbindBuffer(target: Int): Unit = ()
      override def deleteBuffer(buffer: Int): Unit = ()
      override def bufferDataFloat(target: Int, data: Array[Float], usage: Int): Unit = buffer = data.clone()
      override def bufferDataShort(target: Int, data: Array[Short], usage: Int): Unit = ()
      override def getAttribLocation(program: Int, name: String): Option[Int] = Some(1)
      override def getUniformLocation(program: Int, name: String): Option[Int] = Some(1)
      override def uniformMatrix4fv(location: Int, transpose: Boolean, value: Array[Float]): Unit = matrices :+= value.toVector
      override def uniform1i(location: Int, value: Int): Unit = ()
      override def uniform1f(location: Int, value: Float): Unit = ()
      override def enableVertexAttribArray(index: Int): Unit = ()
      override def vertexAttribPointer(index: Int, size: Int, dataType: Int, normalized: Boolean, vertexStride: Int, offset: Long): Unit = stride = vertexStride
      override def genTexture(): Int = 1
      override def activeTexture(texture: Int): Unit = ()
      override def bindTexture(target: Int, texture: Int): Unit = bound = texture
      override def unbindTexture(target: Int): Unit = bound = 0
      override def deleteTexture(texture: Int): Unit = deleted += texture
      override def texParameteri(target: Int, parameter: Int, value: Int): Unit = ()
      override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = fail("Renderer should load physical images")
      override def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage] = fail("Renderer should load physical images")
      // An image uploaded on its own, as the renderer does for a scope.
      override def loadTextureImage2D(asset: sgl.assets.RawImageAsset): Loader[TextureImage] = {
        val loader = new DefaultLoader[Int]
        nextTexture += 1
        uploads += Upload(asset.resourceName, TextureFilter.Nearest, mipmaps = true, loader, nextTexture)
        loader.loader.map(new TextureImage(_, 1, 1))
      }
      override private[sgl] def loadTexture2DResource(resourceName: String, filter: TextureFilter, generateMipmaps: Boolean): Loader[TextureImage] = {
        val loader = new DefaultLoader[Int]
        nextTexture += 1
        uploads += Upload(resourceName, filter, generateMipmaps, loader, nextTexture)
        loader.loader.map(new TextureImage(_, 1, 1))
      }
      override private[sgl] def maxTextureSize: Int = maxTexture
      override def drawArrays(mode: Int, first: Int, count: Int): Unit = {
        assert(count * (stride / 4) == buffer.length)
        draws += ((bound, buffer))
      }
      override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = {
        meshDraws += ((bound, matrices))
        matrices = Vector.empty
      }
    }
    override val OpenGL: OpenGL = FakeGL
  }

  private val page = "atlas/sprites/mdpi/page-000.png"
  private def atlasDrawable(name: String, x: Int, y: Int, w: Int, h: Int, pageWidth: Int = 64, pageHeight: Int = 32): DrawableAsset =
    AssetFactory.drawableVariants(Vector(
      AssetFactory.atlasVariant("mdpi", s"drawable-mdpi/$name.png", page, pageWidth, pageHeight, x, y, w, h, "nearest")
    ))
  private val coin = atlasDrawable("coin", 2, 2, 16, 16)
  private val gem = atlasDrawable("gem", 20, 2, 8, 12)
  private val standalone = AssetFactory.drawable(Vector("mdpi" -> "drawable-mdpi/hero.png"))

  private def sprite(world: World, texture: Texture2D, z: Float, visual: Texture2D => SpriteVisual = SpriteVisual.Texture(_)): Unit = {
    val obj = world.spawn2D("sprite-" + z)(using world.globalScope)
    obj.attach(new SpriteRenderer(_, visual(texture), 10f, 10f, z))
  }

  private def uvs(vertices: Array[Float]): Vector[(Float, Float)] =
    vertices.grouped(8).map(v => (v(6), v(7))).toVector

  // A world as the runtime sets it up when Render2DModule is installed.
  private def renderWorld(): World = {
    val world = new World
    world.globals.set[Render2DModule.type](Render2DModule)
    world
  }

  // One frame as the runtime draws it: open, let the 2D systems submit, close.
  private def drawFrame(renderer: OpenGLRenderer, world: World): Unit = {
    renderer.beginFrame(world)
    SpriteRenderSystem.submit(world, renderer.render2D)
    TilemapRenderSystem.submit(world, renderer.render2D)
    renderer.endFrame(world)
  }

  // A drawable loaded for good, in a scope of its own that is never destroyed.
  private def load(renderer: OpenGLRenderer, asset: DrawableAsset): Loader[Texture2D] = {
    val world = new World
    renderer.whenReady(renderer.loadTexture2D(asset)(using world.globalScope))
  }

  private def loaded(loader: Loader[Texture2D]): Texture2D = {
    assert(loader.isLoaded)
    loader.value.get.get
  }

  test("drawables on one atlas page share a single upload, even when requested concurrently") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val coinLoader = load(renderer, coin)
    val gemLoader = load(renderer, gem)
    val coinAgain = load(renderer, coin)
    assert(app.uploads.map(u => (u.resourceName, u.filter, u.mipmaps)) == Seq((page, TextureFilter.Nearest, false)))
    assert(!coinLoader.isLoaded && !gemLoader.isLoaded)
    app.uploads.head.complete()
    assert(coinLoader.isLoaded && gemLoader.isLoaded && coinAgain.isLoaded)
    assert(load(renderer, gem).isLoaded)
    assert(app.uploads.size == 1)
  }

  test("standalone drawables keep loading their own image with mipmaps") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    load(renderer, standalone)
    assert(app.uploads.map(u => (u.resourceName, u.mipmaps)) == Seq(("drawable-mdpi/hero.png", true)))
  }

  test("atlas pages larger than the device limit fall back to the standalone image") {
    val app = new FakeGLApp(maxTexture = 32)
    val renderer = new OpenGLRenderer(app)
    load(renderer, coin)
    assert(app.uploads.map(u => (u.resourceName, u.mipmaps)) == Seq(("drawable-mdpi/coin.png", true)))
  }

  test("what a scope loads is released with the scope, and usable by sprites and materials until then") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val scene = world.createScope("scene")

    val texture = renderer.loadTexture2D(AssetFactory.rawImage("images/wood.png"))(using scene)
    val mesh = renderer.createMesh(MeshData(Array.empty[Float], Array.empty[Short]))(using scene)
    val material = renderer.createMaterial(texture, emissive = 0f)(using scene)
    assert(mesh.toString == "Mesh(0)" && material.toString == "Material(0)")
    val ready = renderer.whenReady(texture)
    assert(!ready.isLoaded)
    app.uploads.head.complete()
    assert(ready.value.map(_.get) == Some(texture))
    val uploaded = app.uploads.head.texture

    world.spawn2D("sprite")(using scene).attach(new SpriteRenderer(_, SpriteVisual.Texture(texture), 10f, 10f))
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(uploaded))
    assert(app.deleted.isEmpty)

    world.destroyScope(scene)
    assert(app.deleted.toSeq == Seq(uploaded))
    // Releasing again, by hand, is harmless.
    renderer.releaseTexture(texture)
    renderer.releaseMesh(mesh)
    renderer.releaseMaterial(material)
    assert(app.deleted.toSeq == Seq(uploaded))

    intercept[CompositionError] {
      renderer.loadTexture2D(AssetFactory.rawImage("images/late.png"))(using scene)
    }
    assert(app.uploads.size == 1)
  }

  test("an image is uploaded once and leaves with the last texture that shows it") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val first = world.createScope("first")
    val second = world.createScope("second")
    renderer.loadTexture2D(coin)(using first)
    val gemTexture = renderer.loadTexture2D(gem)(using second)
    assert(app.uploads.size == 1)
    app.uploads.head.complete()

    world.destroyScope(first)
    assert(app.deleted.isEmpty && renderer.whenReady(gemTexture).isLoaded)
    world.destroyScope(second)
    assert(app.deleted.toSeq == Seq(app.uploads.head.texture))
    assert(renderer.whenReady(gemTexture).value.exists(_.isFailure))

    // It is uploaded again for whoever needs it next. One released while it is
    // still uploading is deleted as it arrives, and does not stand in for a newer one.
    val third = world.createScope("third")
    renderer.loadTexture2D(coin)(using third)
    world.destroyScope(third)
    val fourth = world.createScope("fourth")
    val again = renderer.loadTexture2D(coin)(using fourth)
    app.uploads(1).complete()
    assert(app.deleted.last == app.uploads(1).texture && !renderer.whenReady(again).isLoaded)
    app.uploads(2).complete()
    assert(renderer.whenReady(again).isLoaded && app.deleted.size == 2)
  }

  // A world with a 3D camera at the origin, looking along -Z.
  private def world3D(): World = {
    val world = renderWorld()
    world.globals.set[Render3DModule.type](Render3DModule)
    world.spawn3D("camera")(using world.globalScope).attach(new Camera3D(_, 1f, 0.1f, 100f))
    world
  }

  test("a material draws the region of an atlas page its texture is, clamped to its edge texels") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = world3D()
    import world.given
    val mesh = renderer.createMesh(MeshData(Array.empty, Array.empty))
    val coinMaterial = renderer.createMaterial(loadedAfter(app, load(renderer, coin)), emissive = 0f)
    val heroMaterial = renderer.createMaterial(loadedAfter(app, load(renderer, standalone)), emissive = 0f)
    renderer.beginFrame(world)
    renderer.render3D.mesh(mesh, coinMaterial, Mat4.Identity)
    renderer.render3D.mesh(mesh, heroMaterial, Mat4.Identity)
    renderer.endFrame(world)

    assert(app.meshDraws.map(_._1) == app.uploads.map(_.texture))
    assert(app.meshDraws.map(_._2(2).take(8)) == Seq(
      Vector(2f / 64f, 2f / 32f, 16f / 64f, 16f / 32f, 2.5f / 64f, 2.5f / 32f, 17.5f / 64f, 17.5f / 32f),
      Vector(0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f)
    ))
  }

  test("painted 3D drawings go from the farthest to the closest, after meshes and without writing depth") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = world3D()
    var writes = Vector.empty[Boolean]
    var seen = Vector.empty[(Vec3, Vec3, Vec3)]
    def at(z: Float, color: Color): Unit =
      renderer.render3D.painted(Vec3(0f, 0f, z)) { painter =>
        writes :+= app.depthWrites
        seen :+= ((painter.camera, painter.right, painter.up))
        painter.triangle(Vec3(0f, 0f, z), Vec3(1f, 0f, z), Vec3(0f, 1f, z), color)
      }
    renderer.beginFrame(world)
    at(-2f, Color(1f, 0f, 0f))
    at(-9f, Color(0f, 1f, 0f))
    at(-5f, Color(0f, 0f, 1f))
    renderer.endFrame(world)

    assert(app.draws.size == 1)
    assert(app.draws.head._2.grouped(9).grouped(3).map(_.head(2)).toVector == Vector(-9f, -5f, -2f))
    assert(writes == Vector(false, false, false) && app.depthWrites)
    assert(seen.distinct == Vector((Vec3.Zero, Vec3.UnitX, Vec3.UnitY)))

    // Nothing is carried over, and nothing is drawn without a camera.
    renderer.beginFrame(world)
    renderer.endFrame(world)
    val bare = renderWorld()
    renderer.beginFrame(bare)
    at(-2f, Color.White)
    renderer.endFrame(bare)
    assert(app.draws.size == 1)

    // A failing drawing leaves the renderer usable.
    renderer.beginFrame(world)
    renderer.render3D.painted(Vec3.Zero)(_ => throw new IllegalStateException("paint failed"))
    intercept[IllegalStateException](renderer.endFrame(world))
    assert(app.depthWrites)
    renderer.beginFrame(world)
    renderer.endFrame(world)
  }

  test("submitted drawings are composed by z, then screen order, whatever order they arrive in") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val draw = renderer.render2D
    val red = Color(1f, 0f, 0f, 1f)
    val green = Color(0f, 1f, 0f, 1f)
    val blue = Color(0f, 0f, 1f, 1f)
    val white = Color(1f, 1f, 1f, 1f)
    def corner(painter: sgl.engine.gameobject.render2d.Painter2D, color: Color): Unit =
      painter.triangle(Vec2(0f, 0f), Vec2(1f, 0f), Vec2(0f, 1f), color)

    renderer.beginFrame(world)
    draw.screen(order = 5)(corner(_, white))
    draw.world(z = 2f)(corner(_, red))
    draw.screen(order = 1)(corner(_, blue))
    draw.world(z = 1f)(corner(_, green))
    renderer.endFrame(world)

    // Untextured drawings share one draw; each vertex is x, y, r, g, b, a, u, v.
    assert(app.draws.size == 1)
    val vertices = app.draws.head._2.grouped(8).toVector
    val firstOfEach = vertices.grouped(3).map(_.head).toVector
    assert(firstOfEach.map(v => Color(v(2), v(3), v(4), v(5))) == Vector(green, red, blue, white))
    // A world origin is the middle of the view; a screen origin is its top left.
    assert(firstOfEach.map(v => (v(0), v(1))) == Vector((0f, 0f), (0f, 0f), (-1f, 1f), (-1f, 1f)))

    // Nothing is carried over to the next frame.
    renderer.beginFrame(world)
    renderer.endFrame(world)
    assert(app.draws.size == 1)
  }

  test("consecutive sprites on a shared page render in one draw with page-relative UVs") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val coinTexture = load(renderer, coin)
    val gemTexture = load(renderer, gem)
    app.uploads.head.complete()
    sprite(world, loaded(coinTexture), 0f)
    sprite(world, loaded(gemTexture), 1f)
    drawFrame(renderer, world)

    assert(app.draws.size == 1)
    val (texture, vertices) = app.draws.head
    assert(texture == app.uploads.head.texture)
    val coinUvs = uvs(vertices).take(6)
    val gemUvs = uvs(vertices).drop(6)
    assert(coinUvs.head == ((2f / 64f, 2f / 32f)))
    assert(coinUvs(2) == ((18f / 64f, 18f / 32f)))
    assert(gemUvs.head == ((20f / 64f, 2f / 32f)))
    assert(gemUvs(2) == ((28f / 64f, 14f / 32f)))
  }

  test("crops and flips stay relative to the logical image") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val coinTexture = load(renderer, coin)
    app.uploads.head.complete()
    // Right half of the coin, mirrored horizontally.
    sprite(world, loaded(coinTexture), 0f, SpriteVisual.Texture(_, u0 = 1f, v0 = 0f, u1 = 0.5f, v1 = 1f))
    drawFrame(renderer, world)
    val quad = uvs(app.draws.head._2)
    assert(quad.head == ((18f / 64f, 2f / 32f)))
    assert(quad(2) == ((10f / 64f, 18f / 32f)))
  }

  test("draws are batched only when consecutive, so z order is preserved") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    val coinTexture = load(renderer, coin)
    val heroTexture = load(renderer, standalone)
    app.uploads.foreach(_.complete())
    sprite(world, loaded(coinTexture), 0f)
    sprite(world, loaded(heroTexture), 1f)
    sprite(world, loaded(coinTexture), 2f)
    drawFrame(renderer, world)
    val atlasTexture = app.uploads.find(_.resourceName == page).get.texture
    val heroPage = app.uploads.find(_.resourceName == "drawable-mdpi/hero.png").get.texture
    assert(app.draws.map(_._1) == Seq(atlasTexture, heroPage, atlasTexture))
  }

  test("overlapping translucent tilemap quads keep painter order while sharing one draw") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    import world.given
    val coinTexture = loadedAfter(app, load(renderer, coin))
    val gemTexture = load(renderer, gem)
    val obj = world.spawn2D("map")
    obj.attach(new Tilemap(_, Vector(
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 0.5f, 0.5f, alpha = 0.25f),
      TileQuad(loaded(gemTexture), 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.5f),
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0.5f, 0.5f, 1f, 1f, alpha = 0.75f),
    )))
    drawFrame(renderer, world)
    assert(app.draws.size == 1)
    assert(app.draws.head._2.length == 3 * 6 * 8)
    val vertices = app.draws.head._2
    assert(vertices.grouped(6 * 8).map(_(5)).toVector == Vector(0.25f, 0.5f, 0.75f))
    assert(uvs(vertices).grouped(6).map(_.head).toVector == Vector(
      (2f / 64f, 2f / 32f), (20f / 64f, 2f / 32f), (10f / 64f, 10f / 32f)
    ))
  }

  test("tilemap batches flush at intervening pages without changing painter order") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = renderWorld()
    import world.given
    val coinLoader = load(renderer, coin)
    val heroLoader = load(renderer, standalone)
    app.uploads.foreach(_.complete())
    val coinTexture = loaded(coinLoader)
    val heroTexture = loaded(heroLoader)
    world.spawn2D("map").attach(new Tilemap(_, Vector(
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.25f),
      TileQuad(heroTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.5f),
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.75f)
    )))
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(app.uploads(0).texture, app.uploads(1).texture, app.uploads(0).texture))
    assert(app.draws.map(_._2(5)) == Seq(0.25f, 0.5f, 0.75f))
  }

  private def loadedAfter(app: FakeGLApp, loader: Loader[Texture2D]): Texture2D = {
    app.uploads.foreach(u => if(!u.loader.isLoaded) u.complete())
    loaded(loader)
  }

  test("a shared page is deleted once on dispose, and late uploads are deleted too") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    load(renderer, coin)
    load(renderer, gem)
    load(renderer, standalone)
    val atlasUpload = app.uploads.find(_.resourceName == page).get
    val heroUpload = app.uploads.find(_.resourceName != page).get
    atlasUpload.complete()
    renderer.dispose()
    assert(app.deleted == Seq(atlasUpload.texture))
    heroUpload.complete()
    assert(app.deleted == Seq(atlasUpload.texture, heroUpload.texture))
  }

  test("a hot reload uploads a changed atlas page again and swaps it under every texture cut from it") {
    val app = new FakeGLApp
    val hub = new AssetReloadHub()
    val renderer = new OpenGLRenderer(app, hub)
    val world = renderWorld()
    val coinTexture = loadedAfter(app, load(renderer, coin))
    val gemTexture = loadedAfter(app, load(renderer, gem))
    sprite(world, coinTexture, 0f)
    sprite(world, gemTexture, 1f)
    val first = app.uploads.head.texture

    // Another file: nothing is uploaded.
    hub.reload(AssetChange.paths(Seq("drawable-mdpi/other.png")))
    hub.drain()
    assert(app.uploads.size == 1)

    val report = hub.reload(AssetChange.paths(Seq(page)))
    hub.drain()
    assert(app.uploads.map(_.resourceName) == Seq(page, page))
    // Until the new page has loaded, the old one is drawn.
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(first))
    assert(!report.isLoaded)

    app.uploads.last.complete()
    val second = app.uploads.last.texture
    assert(app.deleted == Seq(first))
    assert(report.value.exists(_.toOption.exists(_.ok)))
    app.draws.clear()
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(second))
  }

  test("a hot reload swaps a standalone texture, and a failed reload keeps the old image") {
    val app = new FakeGLApp
    val hub = new AssetReloadHub()
    val renderer = new OpenGLRenderer(app, hub)
    val world = renderWorld()
    val scene = world.createScope("scene")
    val texture = renderer.loadTexture2D(AssetFactory.rawImage("images/wood.png"))(using scene)
    app.uploads.head.complete()
    sprite(world, texture, 0f)
    val first = app.uploads.head.texture

    val failing = hub.reload(AssetChange.paths(Seq("images/wood.png")))
    hub.drain()
    assert(app.uploads.size == 2)
    app.uploads.last.loader.failure(new RuntimeException("not a PNG"))
    assert(failing.value.get.get.failures.map(_.name) == Vector("images/wood.png"))
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(first))
    assert(app.deleted.isEmpty)

    hub.reload(AssetChange.everything)
    hub.drain()
    app.uploads.last.complete()
    val second = app.uploads.last.texture
    assert(app.deleted == Seq(first))
    app.draws.clear()
    drawFrame(renderer, world)
    assert(app.draws.map(_._1) == Seq(second))

    // Released with the scope; later changes upload nothing.
    world.destroyScope(scene)
    assert(app.deleted == Seq(first, second))
    hub.reload(AssetChange.everything)
    hub.drain()
    assert(app.uploads.size == 3)
  }

  test("when page reloads overlap only the latest upload is kept, and dispose stops reloading") {
    val app = new FakeGLApp
    val hub = new AssetReloadHub()
    val renderer = new OpenGLRenderer(app, hub)
    loadedAfter(app, load(renderer, coin))
    hub.reloadAll()
    hub.drain()
    hub.reloadAll()
    hub.drain()
    val older = app.uploads(1)
    val newer = app.uploads(2)
    newer.complete()
    older.complete()
    assert(app.deleted == Seq(app.uploads.head.texture, older.texture))
    renderer.dispose()
    hub.reloadAll()
    hub.drain()
    assert(app.uploads.size == 3)
  }
}
