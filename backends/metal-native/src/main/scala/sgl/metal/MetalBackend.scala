package sgl
package metal

import _root_.sgl._
import _root_.sgl.assets.{DrawableAsset, RawImageAsset}
import _root_.sgl.util.{Loader, NoLoggingProvider, SingleThreadSchedulerProvider}

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.collection.mutable
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern
private[metal] object MetalBridge {
  def sgl_metal_attach_layer(layer: Ptr[Byte], displayScale: CFloat): Unit = extern
  def sgl_metal_resize_layer(width: CInt, height: CInt, displayScale: CFloat): Unit = extern
  def sgl_metal_set_safe_area(left: CInt, top: CInt, right: CInt, bottom: CInt): Unit = extern
  def sgl_metal_safe_area(edge: CInt): CInt = extern
  def sgl_metal_init(width: CInt, height: CInt, title: CString): CInt = extern
  def sgl_metal_poll_events(): CInt = extern
  def sgl_metal_next_event(eventType: Ptr[CInt], a: Ptr[CInt], b: Ptr[CInt], c: Ptr[CInt]): CInt = extern
  def sgl_metal_width(): CInt = extern
  def sgl_metal_height(): CInt = extern
  def sgl_metal_scale(): CFloat = extern
  def sgl_metal_begin_frame(): CInt = extern
  def sgl_metal_draw(vertices: Ptr[CFloat], vertexCount: CInt, texture: Ptr[Byte]): Unit = extern
  def sgl_metal_draw_clip(vertices: Ptr[CFloat], vertexCount: CInt, texture: Ptr[Byte], depthEnabled: CInt): Unit = extern
  def sgl_metal_set_scissor(enabled: CInt, x: CInt, y: CInt, width: CInt, height: CInt): Unit = extern
  def sgl_metal_read_frame(width: Ptr[CInt], height: Ptr[CInt]): Ptr[Byte] = extern
  def sgl_metal_end_frame(): Unit = extern
  def sgl_metal_create_texture(width: CInt, height: CInt, rgba: Ptr[Byte], linear: CInt): Ptr[Byte] = extern
  def sgl_metal_load_texture(path: CString, width: Ptr[CInt], height: Ptr[CInt]): Ptr[Byte] = extern
  def sgl_metal_set_texture_linear(texture: Ptr[Byte], linear: CInt): Unit = extern
  def sgl_metal_delete_texture(texture: Ptr[Byte]): Unit = extern
  def sgl_metal_open_url(url: CString): CInt = extern
  def sgl_audio_session_set_active(active: CInt): CInt = extern
  def sgl_audio_create(path: CString): Ptr[Byte] = extern
  def sgl_audio_delete(asset: Ptr[Byte]): Unit = extern
  def sgl_audio_play(asset: Ptr[Byte], loops: CInt, rate: CFloat, volume: CFloat): Ptr[Byte] = extern
  def sgl_audio_stop(playback: Ptr[Byte]): Unit = extern
  def sgl_audio_pause(playback: Ptr[Byte]): Unit = extern
  def sgl_audio_resume(playback: Ptr[Byte]): Unit = extern
  def sgl_audio_is_playing(playback: Ptr[Byte]): CInt = extern
  def sgl_audio_set_volume(playback: Ptr[Byte], volume: CFloat): Unit = extern
  def sgl_audio_set_loops(playback: Ptr[Byte], loops: CInt): Unit = extern
  def sgl_audio_delete_playback(playback: Ptr[Byte]): Unit = extern
  def sgl_metal_shutdown(): Unit = extern
}

trait MetalSystemProvider extends SystemProvider with PartsResourcePathProvider {
  object MetalSystem extends System {
    override def exit(): Unit = sys.exit()
    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    override def loadText(asset: assets.TextAsset): Loader[Array[String]] = {
      val file = Paths.get(metalAssetPath(asset.resourceName))
      if(!Files.exists(file)) Loader.failed(new RuntimeException(s"Resource not found: $file"))
      else Loader.successful(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\\r?\\n"))
    }

    override def loadBinary(asset: assets.BinaryAsset): Loader[Array[Byte]] = {
      val file = Paths.get(metalAssetPath(asset.resourceName))
      if(!Files.exists(file)) Loader.failed(new RuntimeException(s"Resource not found: $file"))
      else Loader.successful(Files.readAllBytes(file))
    }

    override def openWebpage(uri: URI): Unit = Zone.acquire { implicit zone =>
      if(MetalBridge.sgl_metal_open_url(toCString(uri.toString)) == 0)
        throw new RuntimeException(s"Could not open webpage: $uri")
    }
  }

  override val System: MetalSystem.type = MetalSystem
  protected val MetalAssetsRoot: String = "assets"
  private[metal] def metalAssetPath(resourceName: String): String =
    if(MetalAssetsRoot.isEmpty) resourceName else MetalAssetsRoot.stripSuffix("/") + "/" + resourceName.stripPrefix("/")

  override val ResourcesRoot: ResourcePath = PartsResourcePath(MetalAssetsRoot.split('/').filter(_.nonEmpty).toVector)
  override val MultiDPIResourcesRoot: ResourcePath =
    PartsResourcePath((MetalAssetsRoot.stripSuffix("/") + "/drawable-mdpi").split('/').filter(_.nonEmpty).toVector)
}

trait MetalWindowProvider extends WindowProvider {
  class MetalWindow extends AbstractWindow {
    override def width: Int = MetalBridge.sgl_metal_width()
    override def height: Int = MetalBridge.sgl_metal_height()
    override def xppi: Float = 160f * MetalBridge.sgl_metal_scale()
    override def yppi: Float = 160f * MetalBridge.sgl_metal_scale()
    override def logicalPpi: Float = 160f
    override def safeArea: Insets = Insets(
      MetalBridge.sgl_metal_safe_area(0),
      MetalBridge.sgl_metal_safe_area(1),
      MetalBridge.sgl_metal_safe_area(2),
      MetalBridge.sgl_metal_safe_area(3),
    )
  }
  override type Window = MetalWindow
  override val Window: Window = new MetalWindow
}

/** Implements the portable OpenGL subset used internally by OpenGLCanvasProvider
  * with a fixed Metal render pipeline. It intentionally does not expose general
  * GLSL execution; games should consume [[MetalCanvasProvider]] instead.
  */
trait MetalCanvasOpenGLProvider extends OpenGLProvider {
  this: MetalSystemProvider with MetalWindowProvider =>

  override def runOnOpenGLThread(task: => Unit): Unit = task

  object MetalCanvasOpenGL extends OpenGL {
    type Shader = Int
    type Program = Int
    type Buffer = Int
    type VertexArray = Int
    type Texture = Ptr[Byte]
    type UniformLocation = Int
    type AttributeLocation = Int
    type ShaderType = Int
    type ShaderParameter = Int
    type ProgramParameter = Int
    type Capability = Int
    type Face = Int
    type BlendFactor = Int
    type BufferTarget = Int
    type BufferUsage = Int
    type TextureTarget = Int
    type TextureUnit = Int
    type TextureParameter = Int
    type TextureParameterValue = Int
    type DataType = Int
    type DrawMode = Int
    type ClearMask = Int

    override val ShaderHeader = ""
    override val ColorBufferBit = 1
    override val DepthBufferBit = 2
    override val FloatType = 1
    override val UnsignedShortType = 2
    override val Triangles = 3
    override val DepthTest = 4
    override val CullFace = 5
    override val Back = 6
    override val Blend = 7
    override val ScissorTest = 8
    override val SrcAlpha = 9
    override val OneMinusSrcAlpha = 10
    override val VertexShader = 11
    override val FragmentShader = 12
    override val CompileStatus = 13
    override val LinkStatus = 14
    override val ArrayBuffer = 15
    override val ElementArrayBuffer = 16
    override val StaticDraw = 17
    override val Texture2D = 18
    override val Texture0 = 19
    override val TextureMinFilter = 20
    override val TextureMagFilter = 21
    override val TextureWrapS = 22
    override val TextureWrapT = 23
    override val Linear = 24
    override val Nearest = 25
    override val ClampToEdge = 26

    private final case class BufferData(var floats: Array[Float] = Array.empty, var shorts: Array[Short] = Array.empty)
    private final case class Attribute(var enabled: Boolean = false, var size: Int = 0, var stride: Int = 0, var offset: Long = 0L, var buffer: Int = 0)
    private final class VertexArrayState {
      val attributes = mutable.Map.empty[Int, Attribute]
      var elementBuffer = 0
    }
    private final class ProgramState {
      val shaderSources = mutable.ArrayBuffer.empty[String]
      val attributeNames = mutable.Map.empty[Int, String]
      val uniformNames = mutable.Map.empty[Int, String]
      val matrices = mutable.Map.empty[String, Array[Float]]
      val floats = mutable.Map.empty[String, Float]
      val ints = mutable.Map.empty[String, Int]
    }

    private var nextHandle = 1
    private var nextLocation = 1
    private val shaderSources = mutable.Map.empty[Int, String]
    private val programs = mutable.Map.empty[Int, ProgramState]
    private val buffers = mutable.Map.empty[Int, BufferData]
    private val vertexArrays = mutable.Map.empty[Int, VertexArrayState]
    private var boundArrayBuffer = 0
    private var boundVertexArray = 0
    private var currentProgram = 0
    private var texture: Texture = null
    private var depthEnabled = false
    private var clearR = 0f
    private var clearG = 0f
    private var clearB = 0f
    private var clearA = 1f

    private def handle(): Int = { val result = nextHandle; nextHandle += 1; result }
    private def location(): Int = { val result = nextLocation; nextLocation += 1; result }
    private def vao: VertexArrayState = vertexArrays.getOrElseUpdate(boundVertexArray, new VertexArrayState)
    private def program: ProgramState = programs.getOrElseUpdate(currentProgram, new ProgramState)
    override def clearMask(mask: Int, masks: Int*): Int = masks.foldLeft(mask)(_ | _)
    override def viewport(x: Int, y: Int, width: Int, height: Int): Unit = ()
    override def enable(capability: Int): Unit = {
      if(capability == DepthTest) depthEnabled = true
      else if(capability == Blend) ()
    }
    override def disable(capability: Int): Unit = {
      if(capability == ScissorTest) MetalBridge.sgl_metal_set_scissor(0, 0, 0, 0, 0)
      else if(capability == DepthTest) depthEnabled = false
      else if(capability == Blend) ()
    }
    override def cullFace(mode: Int): Unit = ()
    override def blendFunc(source: Int, destination: Int): Unit = ()
    override def scissor(x: Int, y: Int, width: Int, height: Int): Unit =
      MetalBridge.sgl_metal_set_scissor(1, x, Window.height - y - height, width, height)
    override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = {
      clearR = red; clearG = green; clearB = blue; clearA = alpha
    }
    override def clear(mask: Int): Unit = if((mask & ColorBufferBit) != 0) {
      val vertex = Array[Float](0f, 0f, 0f, 1f, 0f, 0f, clearR, clearG, clearB, clearA, 0f)
      val positions = Vector((-1f, 1f), (-1f, -1f), (1f, -1f), (-1f, 1f), (1f, -1f), (1f, 1f))
      val packed = positions.flatMap { case (x, y) => vertex.updated(0, x).updated(1, y) }.toArray
      val nativeVertices = stdlib.malloc((packed.length * 4).toUSize).asInstanceOf[Ptr[CFloat]]
      if(nativeVertices == null) throw new OutOfMemoryError("Could not allocate Metal clear vertices")
      try {
        var index = 0
        while(index < packed.length) { nativeVertices(index) = packed(index); index += 1 }
        MetalBridge.sgl_metal_draw_clip(nativeVertices, 6, null, 0)
      } finally stdlib.free(nativeVertices)
    }

    override def createShader(shaderType: Int): Int = {
      val result = handle(); shaderSources(result) = ""; result
    }
    override def shaderSource(shader: Int, source: String): Unit = shaderSources(shader) = source
    override def compileShader(shader: Int): Unit = ()
    override def getShaderParameter(shader: Int, parameter: Int): Int = 1
    override def getShaderInfoLog(shader: Int): String = ""
    override def deleteShader(shader: Int): Unit = shaderSources.remove(shader)
    override def createProgram(): Int = {
      val result = handle(); programs(result) = new ProgramState; result
    }
    override def attachShader(programHandle: Int, shader: Int): Unit =
      programs(programHandle).shaderSources += shaderSources.getOrElse(shader, "")
    override def linkProgram(program: Int): Unit = ()
    override def getProgramParameter(program: Int, parameter: Int): Int = 1
    override def getProgramInfoLog(program: Int): String = ""
    override def useProgram(program: Int): Unit = currentProgram = program
    override def useNoProgram(): Unit = currentProgram = 0
    override def deleteProgram(program: Int): Unit = programs.remove(program)
    override def genVertexArray(): Int = {
      val result = handle(); vertexArrays(result) = new VertexArrayState; result
    }
    override def bindVertexArray(vertexArray: Int): Unit = boundVertexArray = vertexArray
    override def unbindVertexArray(): Unit = boundVertexArray = 0
    override def deleteVertexArray(vertexArray: Int): Unit = vertexArrays.remove(vertexArray)
    override def genBuffer(): Int = {
      val result = handle(); buffers(result) = BufferData(); result
    }
    override def bindBuffer(target: Int, buffer: Int): Unit = {
      if(target == ArrayBuffer) boundArrayBuffer = buffer else if(target == ElementArrayBuffer) vao.elementBuffer = buffer
    }
    override def unbindBuffer(target: Int): Unit = {
      if(target == ArrayBuffer) boundArrayBuffer = 0 else if(target == ElementArrayBuffer) vao.elementBuffer = 0
    }
    override def deleteBuffer(buffer: Int): Unit = buffers.remove(buffer)
    override def bufferDataFloat(target: Int, data: Array[Float], usage: Int): Unit = {
      val handle = if(target == ArrayBuffer) boundArrayBuffer else vao.elementBuffer
      buffers.getOrElseUpdate(handle, BufferData()).floats = data.clone()
    }
    override def bufferDataShort(target: Int, data: Array[Short], usage: Int): Unit = {
      val handle = if(target == ArrayBuffer) boundArrayBuffer else vao.elementBuffer
      buffers.getOrElseUpdate(handle, BufferData()).shorts = data.clone()
    }
    override def getAttribLocation(programHandle: Int, name: String): Option[Int] = {
      val state = programs(programHandle)
      state.attributeNames.find(_._2 == name).map(_._1).orElse {
        val result = location(); state.attributeNames(result) = name; Some(result)
      }
    }
    override def getUniformLocation(programHandle: Int, name: String): Option[Int] = {
      val state = programs(programHandle)
      state.uniformNames.find(_._2 == name).map(_._1).orElse {
        val result = location(); state.uniformNames(result) = name; Some(result)
      }
    }
    private def uniformName(index: Int): String = program.uniformNames.getOrElse(index, "")
    override def uniformMatrix4fv(location: Int, transpose: Boolean, value: Array[Float]): Unit = program.matrices(uniformName(location)) = value.clone()
    override def uniform1i(location: Int, value: Int): Unit = program.ints(uniformName(location)) = value
    override def uniform1f(location: Int, value: Float): Unit = program.floats(uniformName(location)) = value
    override def enableVertexAttribArray(index: Int): Unit = vao.attributes.getOrElseUpdate(index, Attribute()).enabled = true
    override def vertexAttribPointer(index: Int, size: Int, dataType: Int, normalized: Boolean, stride: Int, offset: Long): Unit = {
      val attribute = vao.attributes.getOrElseUpdate(index, Attribute())
      attribute.size = size; attribute.stride = stride; attribute.offset = offset; attribute.buffer = boundArrayBuffer
    }
    override def genTexture(): Texture = null
    override def activeTexture(texture: Int): Unit = ()
    override def bindTexture(target: Int, newTexture: Texture): Unit = texture = newTexture
    override def unbindTexture(target: Int): Unit = texture = null
    override def deleteTexture(texture: Texture): Unit = if(texture != null) MetalBridge.sgl_metal_delete_texture(texture)
    override def texParameteri(target: Int, parameter: Int, value: Int): Unit =
      if(texture != null && (parameter == TextureMinFilter || parameter == TextureMagFilter))
        MetalBridge.sgl_metal_set_texture_linear(texture, if(value == Linear) 1 else 0)

    override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = {
      require(width > 0 && height > 0)
      require(rgba.length == width * height * 4)
      val pixels = stdlib.malloc(rgba.length.toUSize).asInstanceOf[Ptr[Byte]]
      if(pixels == null) throw new OutOfMemoryError("Could not allocate Metal texture data")
      try {
        var index = 0
        while(index < rgba.length) { pixels(index) = rgba(index); index += 1 }
        val handle = MetalBridge.sgl_metal_create_texture(width, height, pixels, 0)
        if(handle == null) throw new RuntimeException("Could not create Metal texture")
        new TextureImage(handle, width, height)
      } finally stdlib.free(pixels)
    }

    override def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage] = {
      val variant = asset.bestVariantForDpi(Window.logicalPpi)
      loadTexture(variant.resourceName)
    }
    override def loadTextureImage2D(asset: RawImageAsset): Loader[TextureImage] = loadTexture(asset.resourceName)

    private def loadTexture(resourceName: String): Loader[TextureImage] = Zone.acquire { implicit zone =>
      val width = stackalloc[CInt]()
      val height = stackalloc[CInt]()
      val path = metalAssetPath(resourceName)
      val handle = MetalBridge.sgl_metal_load_texture(toCString(path), width, height)
      if(handle == null) Loader.failed(new RuntimeException(s"Could not load Metal texture: $path"))
      else Loader.successful(new TextureImage(handle, !width, !height))
    }

    override def drawArrays(mode: Int, first: Int, count: Int): Unit = {
      require(mode == Triangles)
      drawExpanded((first until first + count).toArray)
    }

    override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = {
      require(mode == Triangles && dataType == UnsignedShortType)
      val indices = buffers.getOrElse(vao.elementBuffer, BufferData()).shorts
      val start = (offset / 2L).toInt
      drawExpanded(indices.slice(start, start + count).map(_ & 0xffff))
    }

    private def drawExpanded(indices: Array[Int]): Unit = {
      if(indices.isEmpty || currentProgram == 0) return
      val state = program
      val byName = state.attributeNames.map { case (index, name) => name -> vao.attributes.getOrElse(index, Attribute()) }.toMap
      val position = byName.get("a_position") match {
        case Some(value) => value
        case None => return
      }
      if(buffers.getOrElse(position.buffer, BufferData()).floats.isEmpty) return

      def component(attribute: Attribute, vertex: Int, componentIndex: Int, default: Float): Float = {
        if(!attribute.enabled || attribute.buffer == 0 || componentIndex >= attribute.size) default
        else {
          val values = buffers.getOrElse(attribute.buffer, BufferData()).floats
          val stride = if(attribute.stride == 0) attribute.size else attribute.stride / 4
          val index = vertex * stride + (attribute.offset / 4L).toInt + componentIndex
          if(index >= 0 && index < values.length) values(index) else default
        }
      }
      def matrix(name: String): Array[Float] = state.matrices.getOrElse(name, Array[Float](1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1))
      def transform(m: Array[Float], x: Float, y: Float, z: Float): (Float, Float, Float, Float) = (
        m(0)*x + m(4)*y + m(8)*z + m(12),
        m(1)*x + m(5)*y + m(9)*z + m(13),
        m(2)*x + m(6)*y + m(10)*z + m(14),
        m(3)*x + m(7)*y + m(11)*z + m(15),
      )

      val normal = byName.get("a_normal")
      val color = byName.get("a_color")
      val uv = byName.get("a_uv").orElse(byName.get("a_texcoord"))
      val isCanvas = byName.contains("a_textured")
      val mvp = matrix("u_mvp")
      val model = matrix("u_model")
      val useTexture = state.floats.get("u_use_texture").orElse(state.floats.get("u_useTexture")).getOrElse(if(texture != null) 1f else 0f)
      val emissive = state.floats.getOrElse("u_emissive", 0f)
      val packed = new Array[Float](indices.length * 11)
      var output = 0
      indices.foreach { vertex =>
        val px = component(position, vertex, 0, 0f)
        val py = component(position, vertex, 1, 0f)
        val pz = component(position, vertex, 2, 0f)
        val clip = if(position.size >= 3) transform(mvp, px, py, pz) else (px, py, 0f, 1f)
        val baseColor = color.map(a => (
          component(a, vertex, 0, 1f), component(a, vertex, 1, 1f),
          component(a, vertex, 2, 1f), component(a, vertex, 3, 1f),
        )).getOrElse((1f, 1f, 1f, 1f))
        val light = normal.map { n =>
          val nx = component(n, vertex, 0, 0f); val ny = component(n, vertex, 1, 0f); val nz = component(n, vertex, 2, 1f)
          val tx = model(0)*nx + model(4)*ny + model(8)*nz
          val ty = model(1)*nx + model(5)*ny + model(9)*nz
          val tz = model(2)*nx + model(6)*ny + model(10)*nz
          val length = scala.math.sqrt(tx*tx + ty*ty + tz*tz).toFloat max 0.0001f
          val dot = scala.math.max(0f, (tx/length) * -0.35f + (ty/length) * 0.5f + (tz/length))
          scala.math.max(emissive, 0.22f + dot*0.85f).toFloat
        }.getOrElse(1f)
        packed(output) = clip._1; packed(output+1) = clip._2; packed(output+2) = clip._3; packed(output+3) = clip._4
        packed(output+4) = uv.map(component(_, vertex, 0, 0f)).getOrElse(0f)
        packed(output+5) = uv.map(component(_, vertex, 1, 0f)).getOrElse(0f)
        packed(output+6) = baseColor._1*light; packed(output+7) = baseColor._2*light
        packed(output+8) = baseColor._3*light; packed(output+9) = baseColor._4
        packed(output+10) = if(isCanvas) component(byName("a_textured"), vertex, 0, 0f) else useTexture
        output += 11
      }
      val visiblePacked = packed.grouped(33).filter { triangle =>
        def outside(component: Int, negative: Boolean): Boolean = (0 until 3).forall { vertex =>
          val base = vertex*11
          if(negative) triangle(base+component) < -triangle(base+3) else triangle(base+component) > triangle(base+3)
        }
        !(outside(0, true) || outside(0, false) || outside(1, true) || outside(1, false) || outside(2, true) || outside(2, false))
      }.flatten.toArray
      if(visiblePacked.isEmpty) return
      val nativeVertices = stdlib.malloc((visiblePacked.length * 4).toUSize).asInstanceOf[Ptr[CFloat]]
      if(nativeVertices == null) throw new OutOfMemoryError("Could not allocate Metal vertices")
      try {
        var index = 0
        while(index < visiblePacked.length) { nativeVertices(index) = visiblePacked(index); index += 1 }
        MetalBridge.sgl_metal_draw_clip(nativeVertices, visiblePacked.length / 11, texture, if(depthEnabled && position.size >= 3) 1 else 0)
      } finally stdlib.free(nativeVertices)
    }
  }

  override val OpenGL: MetalCanvasOpenGL.type = MetalCanvasOpenGL
}

trait MetalCanvasProvider extends MetalCanvasOpenGLProvider with OpenGLCanvasProvider {
  this: MetalSystemProvider with MetalWindowProvider =>
}

trait MetalAudioProvider extends AudioProvider {
  this: MetalSystemProvider =>

  object MetalAudio extends Audio {
    final class Playback private[MetalAudio] (private[MetalAudio] val handle: Ptr[Byte]) {
      private[MetalAudio] var paused = false
    }

    private val sounds = mutable.Set.empty[Sound]
    private val musics = mutable.Set.empty[Music]

    final class Sound private[MetalAudio] (path: String, loops: Int, rate: Float) extends AbstractSound {
      override type PlayedSound = Playback
      private val asset = Zone.acquire { implicit zone => MetalBridge.sgl_audio_create(toCString(path)) }
      private val active = mutable.Set.empty[Playback]
      private var disposed = false

      if(asset == null) throw new RuntimeException(s"Could not create Metal audio asset: $path")
      sounds += this

      override def play(volume: Float): Option[Playback] = {
        require(volume >= 0f && volume <= 1f, "Volume must be between zero and one")
        if(disposed) None
        else {
          val handle = MetalBridge.sgl_audio_play(asset, loops, rate, volume)
          if(handle == null) None
          else {
            val playback = new Playback(handle)
            active += playback
            Some(playback)
          }
        }
      }

      override def withConfig(loop: Int, newRate: Float): Sound = {
        require(loop >= -1, "Loop count must be -1, zero, or positive")
        require(newRate >= 0.5f && newRate <= 2f, "Playback rate must be between 0.5 and 2.0")
        new Sound(path, loop, newRate)
      }

      override def stop(playback: Playback): Unit = release(playback, stopFirst = true)
      override def pause(playback: Playback): Unit = if(active.contains(playback) && !playback.paused) {
        MetalBridge.sgl_audio_pause(playback.handle)
        playback.paused = true
      }
      override def resume(playback: Playback): Unit = if(active.contains(playback) && playback.paused) {
        MetalBridge.sgl_audio_resume(playback.handle)
        playback.paused = false
      }
      override def endLoop(playback: Playback): Unit = if(active.contains(playback)) MetalBridge.sgl_audio_set_loops(playback.handle, 0)

      private[MetalAudio] def update(): Unit =
        active.filter(playback => !playback.paused && MetalBridge.sgl_audio_is_playing(playback.handle) == 0).toVector.foreach(release(_, stopFirst = false))

      private def release(playback: Playback, stopFirst: Boolean): Unit = if(active.remove(playback)) {
        if(stopFirst) MetalBridge.sgl_audio_stop(playback.handle)
        MetalBridge.sgl_audio_delete_playback(playback.handle)
      }

      override def dispose(): Unit = if(!disposed) {
        active.toVector.foreach(release(_, stopFirst = true))
        MetalBridge.sgl_audio_delete(asset)
        sounds -= this
        disposed = true
      }
    }

    override def loadSound(asset: assets.AudioAsset, extras: assets.AudioAsset*): Loader[Sound] = {
      val path = metalAssetPath(asset.resourceName)
      if(!Files.exists(Paths.get(path))) Loader.failed(new RuntimeException(s"Audio resource not found: $path"))
      else Loader.successful(new Sound(path, 0, 1f))
    }

    final class Music private[MetalAudio] (path: String) extends AbstractMusic {
      private val asset = Zone.acquire { implicit zone => MetalBridge.sgl_audio_create(toCString(path)) }
      private var playback: Ptr[Byte] = null
      private var looping = false
      private var volume = 1f
      private var paused = false
      private var disposed = false

      if(asset == null) throw new RuntimeException(s"Could not create Metal music asset: $path")
      musics += this

      override def play(): Unit = if(!disposed) {
        if(playback != null && paused) {
          MetalBridge.sgl_audio_resume(playback)
          paused = false
        } else if(playback == null || MetalBridge.sgl_audio_is_playing(playback) == 0) {
          releasePlayback(stopFirst = false)
          playback = MetalBridge.sgl_audio_play(asset, if(looping) -1 else 0, 1f, volume)
        }
      }
      override def pause(): Unit = if(playback != null && !paused) {
        MetalBridge.sgl_audio_pause(playback)
        paused = true
      }
      override def stop(): Unit = releasePlayback(stopFirst = true)
      override def setVolume(newVolume: Float): Unit = {
        require(newVolume >= 0f && newVolume <= 1f, "Volume must be between zero and one")
        volume = newVolume
        if(playback != null) MetalBridge.sgl_audio_set_volume(playback, newVolume)
      }
      override def setLooping(isLooping: Boolean): Unit = {
        looping = isLooping
        if(playback != null) MetalBridge.sgl_audio_set_loops(playback, if(isLooping) -1 else 0)
      }
      private def releasePlayback(stopFirst: Boolean): Unit = if(playback != null) {
        if(stopFirst) MetalBridge.sgl_audio_stop(playback)
        MetalBridge.sgl_audio_delete_playback(playback)
        playback = null
        paused = false
      }
      override def dispose(): Unit = if(!disposed) {
        releasePlayback(stopFirst = true)
        MetalBridge.sgl_audio_delete(asset)
        musics -= this
        disposed = true
      }
    }

    override def loadMusic(asset: assets.AudioAsset, extras: assets.AudioAsset*): Loader[Music] = {
      val path = metalAssetPath(asset.resourceName)
      if(!Files.exists(Paths.get(path))) Loader.failed(new RuntimeException(s"Music resource not found: $path"))
      else Loader.successful(new Music(path))
    }

    private[metal] def update(): Unit = sounds.toVector.foreach(_.update())
    private[metal] def disposeAll(): Unit = {
      musics.toVector.foreach(_.dispose())
      sounds.toVector.foreach(_.dispose())
    }
  }

  override val Audio: MetalAudio.type = MetalAudio
}

trait MetalApp extends MetalSystemProvider
    with MetalWindowProvider
    with MetalAudioProvider
    with NoLoggingProvider
    with SingleThreadSchedulerProvider
    with FrameCaptureProvider
    with MetalCanvasProvider {
  this: Application =>

  val TargetFps: Option[Int] = Some(60)
  val frameDimension: (Int, Int)
  val windowTitle: String = "SGL Metal App"

  private var embeddedCreated = false
  private var embeddedPaused = false

  def attachEmbeddedMetalLayer(layer: Ptr[Byte], displayScale: Float): Unit =
    MetalBridge.sgl_metal_attach_layer(layer, displayScale)

  def initializeEmbeddedMetal(width: Int, height: Int, displayScale: Float): Boolean = Zone.acquire { implicit zone =>
    MetalBridge.sgl_metal_resize_layer(width, height, displayScale)
    if(embeddedCreated) true
    else if(MetalBridge.sgl_metal_init(width, height, toCString(windowTitle)) == 0) false
    else {
      MetalBridge.sgl_audio_session_set_active(1)
      println(s"Initializing embedded Metal ${Window.width}x${Window.height} scale=${MetalBridge.sgl_metal_scale()}")
      create()
      embeddedCreated = true
      resize(Window.width, Window.height)
      true
    }
  }

  def resizeEmbeddedMetal(width: Int, height: Int, displayScale: Float): Unit = if(embeddedCreated) {
    MetalBridge.sgl_metal_resize_layer(width, height, displayScale)
    resize(Window.width, Window.height)
  }

  def safeAreaEmbeddedMetal(left: Int, top: Int, right: Int, bottom: Int): Unit =
    MetalBridge.sgl_metal_set_safe_area(left, top, right, bottom)

  def frameEmbeddedMetal(deltaSeconds: Double): Unit = {
    if(embeddedCreated && !embeddedPaused && MetalBridge.sgl_metal_begin_frame() != 0) {
      val captureBatch = beginFrameCapture()
      try {
        frame(deltaSeconds)
        MetalAudio.update()
        if(captureBatch.requested) Zone.acquire { implicit zone =>
          val width = stackalloc[CInt](); val height = stackalloc[CInt]()
          val pixels = MetalBridge.sgl_metal_read_frame(width, height)
          if(pixels == null) throw new RuntimeException("Could not read the Metal framebuffer")
          try {
            val size = !width * !height * CapturedFrame.BytesPerPixel
            val rgba = new Array[Byte](size)
            var index = 0
            while(index < size) { rgba(index) = pixels(index); index += 1 }
            completeFrameCapture(captureBatch, new CapturedFrame(!width, !height, rgba))
          } finally stdlib.free(pixels)
        } else MetalBridge.sgl_metal_end_frame()
      } catch {
        case error: Throwable =>
          failFrameCapture(captureBatch, error)
          throw error
      }
    }
  }

  def pauseEmbeddedMetal(): Unit = if(embeddedCreated && !embeddedPaused) {
    pause()
    MetalBridge.sgl_audio_session_set_active(0)
    embeddedPaused = true
  }

  def resumeEmbeddedMetal(): Unit = if(embeddedCreated && embeddedPaused) {
    MetalBridge.sgl_audio_session_set_active(1)
    resume()
    embeddedPaused = false
  }

  def pointerEmbeddedMetal(eventType: Int, x: Int, y: Int, pointerId: Int): Unit = if(embeddedCreated) {
    val button = pointerId match {
      case 1 => Input.MouseButtons.Right
      case 2 => Input.MouseButtons.Middle
      case _ => Input.MouseButtons.Left
    }
    eventType match {
      case 0 => Input.inputProcessor.mouseDown(x, y, button)
      case 1 => Input.inputProcessor.mouseMoved(x, y)
      case 2 | 3 => Input.inputProcessor.mouseUp(x, y, button)
      case _ => ()
    }
  }

  def keyDownEmbeddedMetal(key: Input.Keys.Key): Unit = if(embeddedCreated) Input.inputProcessor.keyDown(key)

  def disposeEmbeddedMetal(): Unit = if(embeddedCreated) {
    try dispose()
    finally {
      MetalAudio.disposeAll()
      MetalBridge.sgl_metal_shutdown()
      embeddedCreated = false
      embeddedPaused = false
    }
  }

  def main(args: Array[String]): Unit = Zone.acquire { implicit zone =>
    if(MetalBridge.sgl_metal_init(frameDimension._1, frameDimension._2, toCString(windowTitle)) == 0)
      throw new RuntimeException("Could not initialize Metal")

    var created = false
    var lastWidth = Window.width
    var lastHeight = Window.height
    var appPaused = false
    try {
      created = true
      create()
      if(java.lang.System.getenv("SGL_CAPTURE_API_SMOKE") != null) {
        captureNextFrame().onLoad {
          case scala.util.Success(frame) => println(s"Captured Metal API frame ${frame.width}x${frame.height} bytes=${frame.rgba.length}")
          case scala.util.Failure(error) => throw error
        }
      }
      resize(lastWidth, lastHeight)
      val targetFrameMillis = TargetFps.map(Application.framePeriodMillis)
      var previous = System.nanoTime
      var running = true
      while(running) {
        val begin = System.nanoTime
        running = MetalBridge.sgl_metal_poll_events() != 0
        if(running) {
          val eventType = stackalloc[CInt](); val a = stackalloc[CInt](); val b = stackalloc[CInt](); val c = stackalloc[CInt]()
          while(MetalBridge.sgl_metal_next_event(eventType, a, b, c) != 0) {
            def key(code: Int): Option[Input.Keys.Key] = code match {
              case 32 => Some(Input.Keys.Space)
              case 1001 => Some(Input.Keys.Left); case 1002 => Some(Input.Keys.Up)
              case 1003 => Some(Input.Keys.Right); case 1004 => Some(Input.Keys.Down)
              case value if value >= 65 && value <= 90 => Some(Vector(
                Input.Keys.A, Input.Keys.B, Input.Keys.C, Input.Keys.D, Input.Keys.E, Input.Keys.F, Input.Keys.G,
                Input.Keys.H, Input.Keys.I, Input.Keys.J, Input.Keys.K, Input.Keys.L, Input.Keys.M, Input.Keys.N,
                Input.Keys.O, Input.Keys.P, Input.Keys.Q, Input.Keys.R, Input.Keys.S, Input.Keys.T, Input.Keys.U,
                Input.Keys.V, Input.Keys.W, Input.Keys.X, Input.Keys.Y, Input.Keys.Z)(value - 65))
              case value if value >= 48 && value <= 57 => Some(Vector(
                Input.Keys.Num0, Input.Keys.Num1, Input.Keys.Num2, Input.Keys.Num3, Input.Keys.Num4,
                Input.Keys.Num5, Input.Keys.Num6, Input.Keys.Num7, Input.Keys.Num8, Input.Keys.Num9)(value - 48))
              case _ => None
            }
            def button(value: Int): Input.MouseButtons.MouseButton = value match {
              case 1 => Input.MouseButtons.Right; case 2 => Input.MouseButtons.Middle; case _ => Input.MouseButtons.Left
            }
            (!eventType) match {
              case 1 => key(!a).foreach(Input.inputProcessor.keyDown)
              case 2 => key(!a).foreach(Input.inputProcessor.keyUp)
              case 3 => Input.inputProcessor.mouseMoved(!a, !b)
              case 4 => Input.inputProcessor.mouseDown(!a, !b, button(!c))
              case 5 => Input.inputProcessor.mouseUp(!a, !b, button(!c))
              case 6 => Input.inputProcessor.mouseScrolled(!a)
              case 8 =>
                lastWidth = Window.width; lastHeight = Window.height; resize(lastWidth, lastHeight)
              case 9 if !appPaused => pause(); appPaused = true
              case 10 if appPaused => resume(); appPaused = false
              case _ => ()
            }
          }
        }
        if(running && !appPaused && MetalBridge.sgl_metal_begin_frame() != 0) {
          val now = System.nanoTime
          val dt = (now - previous).toDouble / 1000000000.0
          previous = now
          val captureBatch = beginFrameCapture()
          try {
            frame(dt)
            MetalAudio.update()
            if(captureBatch.requested) {
              val width = stackalloc[CInt](); val height = stackalloc[CInt]()
              val pixels = MetalBridge.sgl_metal_read_frame(width, height)
              if(pixels == null) throw new RuntimeException("Could not read the Metal framebuffer")
              try {
                val size = !width * !height * CapturedFrame.BytesPerPixel
                val rgba = new Array[Byte](size)
                var index = 0
                while(index < size) { rgba(index) = pixels(index); index += 1 }
                completeFrameCapture(captureBatch, new CapturedFrame(!width, !height, rgba))
              } finally stdlib.free(pixels)
            } else MetalBridge.sgl_metal_end_frame()
          } catch {
            case error: Throwable =>
              failFrameCapture(captureBatch, error)
              throw error
          }
        }
        val elapsedMillis = (System.nanoTime - begin) / 1000000L
        val schedulerMillis = targetFrameMillis.map(_ - elapsedMillis).getOrElse(10L)
        Scheduler.run(schedulerMillis)
        val sleepMillis = targetFrameMillis.map(_ - (System.nanoTime - begin) / 1000000L).getOrElse(0L)
        if(sleepMillis > 0) Thread.sleep(sleepMillis)
      }
    } finally {
      if(created) dispose()
      MetalAudio.disposeAll()
      MetalBridge.sgl_metal_shutdown()
    }
  }
}
