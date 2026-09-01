package sgl

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.util.Loader

/** Provides a small, portable OpenGL ES 3.0 / WebGL 2 style rendering API.
  *
  * This provider intentionally models the common subset shared by Android GLES,
  * WebGL 2, and native OpenGL ES. It is lower-level than [[CanvasProvider]] and
  * meant for games/renderers that want direct GPU control.
  *
  * Most OpenGL/WebGL objects and enum families are represented with abstract
  * types. Backends can map them to their native representation, while portable
  * game code gets compile-time protection against mixing unrelated handles such
  * as programs, shaders, textures, and buffers.
  */
trait OpenGLProvider {
  this: SystemProvider =>

  /** Run a task on the thread/context that owns this OpenGL/WebGL context.
    *
    * Desktop backends with strict context affinity override this and enqueue the
    * task for the render loop. Single-threaded/browser backends can execute it
    * immediately.
    */
  def runOnOpenGLThread(task: => Unit): Unit = task

  trait OpenGL {
    type Shader
    type Program
    type Buffer
    type VertexArray
    type Texture
    type UniformLocation
    type AttributeLocation

    type ShaderType
    type ShaderParameter
    type ProgramParameter
    type Capability
    type Face
    type BlendFactor
    type BufferTarget
    type BufferUsage
    type TextureTarget
    type TextureUnit
    type TextureParameter
    type TextureParameterValue
    type DataType
    type DrawMode
    type ClearMask

    val ShaderHeader: String

    val ColorBufferBit: ClearMask
    val DepthBufferBit: ClearMask

    val FloatType: DataType
    val UnsignedShortType: DataType
    val Triangles: DrawMode

    val DepthTest: Capability
    val CullFace: Capability
    val Back: Face
    val Blend: Capability
    val ScissorTest: Capability
    val SrcAlpha: BlendFactor
    val OneMinusSrcAlpha: BlendFactor

    val VertexShader: ShaderType
    val FragmentShader: ShaderType
    val CompileStatus: ShaderParameter
    val LinkStatus: ProgramParameter

    val ArrayBuffer: BufferTarget
    val ElementArrayBuffer: BufferTarget
    val StaticDraw: BufferUsage

    val Texture2D: TextureTarget
    val Texture0: TextureUnit
    val TextureMinFilter: TextureParameter
    val TextureMagFilter: TextureParameter
    val TextureWrapS: TextureParameter
    val TextureWrapT: TextureParameter
    val Linear: TextureParameterValue
    val Nearest: TextureParameterValue
    val ClampToEdge: TextureParameterValue

    def clearMask(mask: ClearMask, masks: ClearMask*): ClearMask

    def viewport(x: Int, y: Int, width: Int, height: Int): Unit
    def enable(capability: Capability): Unit
    def disable(capability: Capability): Unit
    def cullFace(mode: Face): Unit
    def blendFunc(source: BlendFactor, destination: BlendFactor): Unit
    def scissor(x: Int, y: Int, width: Int, height: Int): Unit
    def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit
    def clear(mask: ClearMask): Unit

    def createShader(shaderType: ShaderType): Shader
    def shaderSource(shader: Shader, source: String): Unit
    def compileShader(shader: Shader): Unit
    def getShaderParameter(shader: Shader, parameter: ShaderParameter): Int
    def getShaderInfoLog(shader: Shader): String
    def deleteShader(shader: Shader): Unit

    def createProgram(): Program
    def attachShader(program: Program, shader: Shader): Unit
    def linkProgram(program: Program): Unit
    def getProgramParameter(program: Program, parameter: ProgramParameter): Int
    def getProgramInfoLog(program: Program): String
    def useProgram(program: Program): Unit
    def useNoProgram(): Unit
    def deleteProgram(program: Program): Unit

    def genVertexArray(): VertexArray
    def bindVertexArray(vertexArray: VertexArray): Unit
    def unbindVertexArray(): Unit
    def deleteVertexArray(vertexArray: VertexArray): Unit

    def genBuffer(): Buffer
    def bindBuffer(target: BufferTarget, buffer: Buffer): Unit
    def unbindBuffer(target: BufferTarget): Unit
    def deleteBuffer(buffer: Buffer): Unit
    def bufferDataFloat(target: BufferTarget, data: Array[Float], usage: BufferUsage): Unit
    def bufferDataShort(target: BufferTarget, data: Array[Short], usage: BufferUsage): Unit

    def getAttribLocation(program: Program, name: String): Option[AttributeLocation]
    def getUniformLocation(program: Program, name: String): Option[UniformLocation]
    def uniformMatrix4fv(location: UniformLocation, transpose: Boolean, value: Array[Float]): Unit
    def uniform1i(location: UniformLocation, value: Int): Unit
    def uniform1f(location: UniformLocation, value: Float): Unit
    def enableVertexAttribArray(index: AttributeLocation): Unit
    def vertexAttribPointer(index: AttributeLocation, size: Int, dataType: DataType, normalized: Boolean, stride: Int, offset: Long): Unit

    def genTexture(): Texture
    def activeTexture(texture: TextureUnit): Unit
    def bindTexture(target: TextureTarget, texture: Texture): Unit
    def unbindTexture(target: TextureTarget): Unit
    def deleteTexture(texture: Texture): Unit
    def texParameteri(target: TextureTarget, parameter: TextureParameter, value: TextureParameterValue): Unit

    /** A texture together with the dimensions of its decoded source image. */
    final class TextureImage(val texture: Texture, val width: Int, val height: Int) {
      require(width > 0 && height > 0, "Texture image dimensions must be positive")
    }

    def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage
    def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage]
    def loadTextureImage2D(asset: RawImageAsset): Loader[TextureImage]
    final def loadTexture2D(asset: DrawableAsset): Loader[Texture] = loadTextureImage2D(asset).map(_.texture)
    final def loadTexture2D(asset: RawImageAsset): Loader[Texture] = loadTextureImage2D(asset).map(_.texture)

    def drawArrays(mode: DrawMode, first: Int, count: Int): Unit
    def drawElements(mode: DrawMode, count: Int, dataType: DataType, offset: Long): Unit
  }

  val OpenGL: OpenGL
}
