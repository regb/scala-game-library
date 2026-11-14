package sgl.engine.gameobject

import scala.collection.mutable

import org.scalatest.funsuite.AnyFunSuite

import sgl.{OpenGLProvider, PartsResourcePathProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetFactory, BinaryAsset, DrawableAsset, TextAsset, TextureFilter}
import sgl.engine.gameobject.opengl.OpenGLRenderer
import sgl.engine.gameobject.render2d.{SpriteRenderer, SpriteVisual, Texture2D, TileQuad, Tilemap}
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
      override def uniformMatrix4fv(location: Int, transpose: Boolean, value: Array[Float]): Unit = ()
      override def uniform1i(location: Int, value: Int): Unit = ()
      override def uniform1f(location: Int, value: Float): Unit = ()
      override def enableVertexAttribArray(index: Int): Unit = ()
      override def vertexAttribPointer(index: Int, size: Int, dataType: Int, normalized: Boolean, stride: Int, offset: Long): Unit = ()
      override def genTexture(): Int = 1
      override def activeTexture(texture: Int): Unit = ()
      override def bindTexture(target: Int, texture: Int): Unit = bound = texture
      override def unbindTexture(target: Int): Unit = bound = 0
      override def deleteTexture(texture: Int): Unit = deleted += texture
      override def texParameteri(target: Int, parameter: Int, value: Int): Unit = ()
      override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = fail("Renderer should load physical images")
      override def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage] = fail("Renderer should load physical images")
      override def loadTextureImage2D(asset: sgl.assets.RawImageAsset): Loader[TextureImage] = fail("Renderer should load physical images")
      override private[sgl] def loadTexture2DResource(resourceName: String, filter: TextureFilter, generateMipmaps: Boolean): Loader[TextureImage] = {
        val loader = new DefaultLoader[Int]
        nextTexture += 1
        uploads += Upload(resourceName, filter, generateMipmaps, loader, nextTexture)
        loader.loader.map(new TextureImage(_, 1, 1))
      }
      override private[sgl] def maxTextureSize: Int = maxTexture
      override def drawArrays(mode: Int, first: Int, count: Int): Unit = {
        assert(count * 8 == buffer.length)
        draws += ((bound, buffer))
      }
      override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = ()
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
    val obj = world.createObject2D("sprite-" + z)
    obj.attach(new SpriteRenderer(_, visual(texture), 10f, 10f, z))
  }

  private def uvs(vertices: Array[Float]): Vector[(Float, Float)] =
    vertices.grouped(8).map(v => (v(6), v(7))).toVector

  private def loaded(loader: Loader[Texture2D]): Texture2D = {
    assert(loader.isLoaded)
    loader.value.get.get
  }

  test("drawables on one atlas page share a single upload, even when requested concurrently") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val coinLoader = renderer.loadDrawableTexture(coin)
    val gemLoader = renderer.loadDrawableTexture(gem)
    val coinAgain = renderer.loadDrawableTexture(coin)
    assert(app.uploads.map(u => (u.resourceName, u.filter, u.mipmaps)) == Seq((page, TextureFilter.Nearest, false)))
    assert(!coinLoader.isLoaded && !gemLoader.isLoaded)
    app.uploads.head.complete()
    assert(coinLoader.isLoaded && gemLoader.isLoaded && coinAgain.isLoaded)
    assert(renderer.loadDrawableTexture(gem).isLoaded)
    assert(app.uploads.size == 1)
  }

  test("standalone drawables keep loading their own image with mipmaps") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    renderer.loadDrawableTexture(standalone)
    assert(app.uploads.map(u => (u.resourceName, u.mipmaps)) == Seq(("drawable-mdpi/hero.png", true)))
  }

  test("atlas pages larger than the device limit fall back to the standalone image") {
    val app = new FakeGLApp(maxTexture = 32)
    val renderer = new OpenGLRenderer(app)
    renderer.loadDrawableTexture(coin)
    assert(app.uploads.map(u => (u.resourceName, u.mipmaps)) == Seq(("drawable-mdpi/coin.png", true)))
  }

  test("consecutive sprites on a shared page render in one draw with page-relative UVs") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = new World
    val coinTexture = renderer.loadDrawableTexture(coin)
    val gemTexture = renderer.loadDrawableTexture(gem)
    app.uploads.head.complete()
    sprite(world, loaded(coinTexture), 0f)
    sprite(world, loaded(gemTexture), 1f)
    renderer.render(world)

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
    val world = new World
    val coinTexture = renderer.loadDrawableTexture(coin)
    app.uploads.head.complete()
    // Right half of the coin, mirrored horizontally.
    sprite(world, loaded(coinTexture), 0f, SpriteVisual.Texture(_, u0 = 1f, v0 = 0f, u1 = 0.5f, v1 = 1f))
    renderer.render(world)
    val quad = uvs(app.draws.head._2)
    assert(quad.head == ((18f / 64f, 2f / 32f)))
    assert(quad(2) == ((10f / 64f, 18f / 32f)))
  }

  test("draws are batched only when consecutive, so z order is preserved") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = new World
    val coinTexture = renderer.loadDrawableTexture(coin)
    val heroTexture = renderer.loadDrawableTexture(standalone)
    app.uploads.foreach(_.complete())
    sprite(world, loaded(coinTexture), 0f)
    sprite(world, loaded(heroTexture), 1f)
    sprite(world, loaded(coinTexture), 2f)
    renderer.render(world)
    val atlasTexture = app.uploads.find(_.resourceName == page).get.texture
    val heroPage = app.uploads.find(_.resourceName == "drawable-mdpi/hero.png").get.texture
    assert(app.draws.map(_._1) == Seq(atlasTexture, heroPage, atlasTexture))
  }

  test("overlapping translucent tilemap quads keep painter order while sharing one draw") {
    val app = new FakeGLApp
    val renderer = new OpenGLRenderer(app)
    val world = new World
    val coinTexture = loadedAfter(app, renderer.loadDrawableTexture(coin))
    val gemTexture = renderer.loadDrawableTexture(gem)
    val obj = world.createObject2D("map")
    obj.attach(new Tilemap(_, Vector(
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 0.5f, 0.5f, alpha = 0.25f),
      TileQuad(loaded(gemTexture), 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.5f),
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0.5f, 0.5f, 1f, 1f, alpha = 0.75f),
    )))
    renderer.render(world)
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
    val world = new World
    val coinLoader = renderer.loadDrawableTexture(coin)
    val heroLoader = renderer.loadDrawableTexture(standalone)
    app.uploads.foreach(_.complete())
    val coinTexture = loaded(coinLoader)
    val heroTexture = loaded(heroLoader)
    world.createObject2D("map").attach(new Tilemap(_, Vector(
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.25f),
      TileQuad(heroTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.5f),
      TileQuad(coinTexture, 0f, 0f, 8f, 8f, 0f, 0f, 1f, 1f, alpha = 0.75f)
    )))
    renderer.render(world)
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
    renderer.loadDrawableTexture(coin)
    renderer.loadDrawableTexture(gem)
    renderer.loadDrawableTexture(standalone)
    val atlasUpload = app.uploads.find(_.resourceName == page).get
    val heroUpload = app.uploads.find(_.resourceName != page).get
    atlasUpload.complete()
    renderer.dispose()
    assert(app.deleted == Seq(atlasUpload.texture))
    heroUpload.complete()
    assert(app.deleted == Seq(atlasUpload.texture, heroUpload.texture))
  }
}
