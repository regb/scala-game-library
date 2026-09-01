package sgl.lwjgl

import sgl.{DesktopSystemProvider, OpenGLProvider}
import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.util.{DefaultLoader, Loader}

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.{GL11C, GL13C, GL15C, GL20C, GL30C}
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack

import java.io.ByteArrayOutputStream

import scala.concurrent.Future

trait LwjglOpenGLProvider extends OpenGLProvider {
  this: DesktopSystemProvider with LwjglRenderThreadDispatcher with LwjglWindowProvider =>

  object LwjglOpenGL extends OpenGL {
    type Shader = Int
    type Program = Int
    type Buffer = Int
    type VertexArray = Int
    type Texture = Int
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

    override val ShaderHeader: String = "#version 330 core\n"

    override val ColorBufferBit: Int = GL11C.GL_COLOR_BUFFER_BIT
    override val DepthBufferBit: Int = GL11C.GL_DEPTH_BUFFER_BIT
    override val FloatType: Int = GL11C.GL_FLOAT
    override val UnsignedShortType: Int = GL11C.GL_UNSIGNED_SHORT
    override val Triangles: Int = GL11C.GL_TRIANGLES
    override val DepthTest: Int = GL11C.GL_DEPTH_TEST
    override val CullFace: Int = GL11C.GL_CULL_FACE
    override val Back: Int = GL11C.GL_BACK
    override val Blend: Int = GL11C.GL_BLEND
    override val ScissorTest: Int = GL11C.GL_SCISSOR_TEST
    override val SrcAlpha: Int = GL11C.GL_SRC_ALPHA
    override val OneMinusSrcAlpha: Int = GL11C.GL_ONE_MINUS_SRC_ALPHA
    override val VertexShader: Int = GL20C.GL_VERTEX_SHADER
    override val FragmentShader: Int = GL20C.GL_FRAGMENT_SHADER
    override val CompileStatus: Int = GL20C.GL_COMPILE_STATUS
    override val LinkStatus: Int = GL20C.GL_LINK_STATUS
    override val ArrayBuffer: Int = GL15C.GL_ARRAY_BUFFER
    override val ElementArrayBuffer: Int = GL15C.GL_ELEMENT_ARRAY_BUFFER
    override val StaticDraw: Int = GL15C.GL_STATIC_DRAW
    override val Texture2D: Int = GL11C.GL_TEXTURE_2D
    override val Texture0: Int = GL13C.GL_TEXTURE0
    override val TextureMinFilter: Int = GL11C.GL_TEXTURE_MIN_FILTER
    override val TextureMagFilter: Int = GL11C.GL_TEXTURE_MAG_FILTER
    override val TextureWrapS: Int = GL11C.GL_TEXTURE_WRAP_S
    override val TextureWrapT: Int = GL11C.GL_TEXTURE_WRAP_T
    override val Linear: Int = GL11C.GL_LINEAR
    override val Nearest: Int = GL11C.GL_NEAREST
    override val ClampToEdge: Int = 0x812F

    override def clearMask(mask: ClearMask, masks: ClearMask*): ClearMask = masks.foldLeft(mask)(_ | _)

    override def viewport(x: Int, y: Int, width: Int, height: Int): Unit = GL11C.glViewport(x, y, width, height)
    override def enable(capability: Int): Unit = GL11C.glEnable(capability)
    override def disable(capability: Int): Unit = GL11C.glDisable(capability)
    override def cullFace(mode: Int): Unit = GL11C.glCullFace(mode)
    override def blendFunc(source: Int, destination: Int): Unit = GL11C.glBlendFunc(source, destination)
    override def scissor(x: Int, y: Int, width: Int, height: Int): Unit = GL11C.glScissor(x, y, width, height)
    override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = GL11C.glClearColor(red, green, blue, alpha)
    override def clear(mask: Int): Unit = GL11C.glClear(mask)

    override def createShader(shaderType: Int): Int = GL20C.glCreateShader(shaderType)
    override def shaderSource(shader: Int, source: String): Unit = GL20C.glShaderSource(shader, source)
    override def compileShader(shader: Int): Unit = GL20C.glCompileShader(shader)
    override def getShaderParameter(shader: Int, parameter: Int): Int = GL20C.glGetShaderi(shader, parameter)
    override def getShaderInfoLog(shader: Int): String = GL20C.glGetShaderInfoLog(shader)
    override def deleteShader(shader: Int): Unit = GL20C.glDeleteShader(shader)

    override def createProgram(): Int = GL20C.glCreateProgram()
    override def attachShader(program: Int, shader: Int): Unit = GL20C.glAttachShader(program, shader)
    override def linkProgram(program: Int): Unit = GL20C.glLinkProgram(program)
    override def getProgramParameter(program: Int, parameter: Int): Int = GL20C.glGetProgrami(program, parameter)
    override def getProgramInfoLog(program: Int): String = GL20C.glGetProgramInfoLog(program)
    override def useProgram(program: Program): Unit = GL20C.glUseProgram(program)
    override def useNoProgram(): Unit = GL20C.glUseProgram(0)
    override def deleteProgram(program: Program): Unit = GL20C.glDeleteProgram(program)

    override def genVertexArray(): VertexArray = GL30C.glGenVertexArrays()
    override def bindVertexArray(vertexArray: VertexArray): Unit = GL30C.glBindVertexArray(vertexArray)
    override def unbindVertexArray(): Unit = GL30C.glBindVertexArray(0)
    override def deleteVertexArray(vertexArray: VertexArray): Unit = GL30C.glDeleteVertexArrays(vertexArray)
    override def genBuffer(): Buffer = GL15C.glGenBuffers()
    override def bindBuffer(target: BufferTarget, buffer: Buffer): Unit = GL15C.glBindBuffer(target, buffer)
    override def unbindBuffer(target: BufferTarget): Unit = GL15C.glBindBuffer(target, 0)
    override def deleteBuffer(buffer: Buffer): Unit = GL15C.glDeleteBuffers(buffer)
    override def bufferDataFloat(target: Int, data: Array[Float], usage: Int): Unit = GL15C.glBufferData(target, data, usage)
    override def bufferDataShort(target: Int, data: Array[Short], usage: Int): Unit = GL15C.glBufferData(target, data, usage)

    override def getAttribLocation(program: Program, name: String): Option[AttributeLocation] = {
      val location = GL20C.glGetAttribLocation(program, name)
      if(location >= 0) Some(location) else None
    }
    override def getUniformLocation(program: Program, name: String): Option[UniformLocation] = {
      val location = GL20C.glGetUniformLocation(program, name)
      if(location >= 0) Some(location) else None
    }
    override def uniformMatrix4fv(location: Int, transpose: Boolean, value: Array[Float]): Unit = GL20C.glUniformMatrix4fv(location, transpose, value)
    override def uniform1i(location: Int, value: Int): Unit = GL20C.glUniform1i(location, value)
    override def uniform1f(location: Int, value: Float): Unit = GL20C.glUniform1f(location, value)
    override def enableVertexAttribArray(index: Int): Unit = GL20C.glEnableVertexAttribArray(index)
    override def vertexAttribPointer(index: Int, size: Int, dataType: Int, normalized: Boolean, stride: Int, offset: Long): Unit = GL20C.glVertexAttribPointer(index, size, dataType, normalized, stride, offset)

    override def genTexture(): Texture = GL11C.glGenTextures()
    override def activeTexture(texture: TextureUnit): Unit = GL13C.glActiveTexture(texture)
    override def bindTexture(target: TextureTarget, texture: Texture): Unit = GL11C.glBindTexture(target, texture)
    override def unbindTexture(target: TextureTarget): Unit = GL11C.glBindTexture(target, 0)
    override def deleteTexture(texture: Texture): Unit = GL11C.glDeleteTextures(texture)
    override def texParameteri(target: TextureTarget, parameter: TextureParameter, value: TextureParameterValue): Unit = GL11C.glTexParameteri(target, parameter, value)

    override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = {
      require(width > 0 && height > 0, "Texture dimensions must be positive")
      require(rgba.length == width * height * 4, "Texture data must contain tightly packed RGBA8 pixels")
      val pixels = BufferUtils.createByteBuffer(rgba.length)
      pixels.put(rgba).flip()
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      GL11C.glTexImage2D(Texture2D, 0, GL11C.GL_RGBA, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixels)
      new TextureImage(texture, width, height)
    }

    override def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage] = {
      val variant = asset.bestVariantForDpi(Window.logicalPpi)
      loadTexture2DFromResourceName(variant.resourceName)
    }

    override def loadTextureImage2D(asset: RawImageAsset): Loader[TextureImage] =
      loadTexture2DFromResourceName(asset.resourceName)

    private def loadTexture2DFromResourceName(resourceName: String): Loader[TextureImage] = {
      val promise = new DefaultLoader[TextureImage]
      Future {
        val decoded = decodeTexture(resourceName)
        try {
          runOnOpenGLThread {
            try {
              val texture = uploadTexture(decoded)
              promise.success(new TextureImage(texture, decoded.width, decoded.height))
            } catch {
              case t: Throwable => promise.failure(t)
            } finally {
              STBImage.stbi_image_free(decoded.pixels)
            }
          }
        } catch {
          case t: Throwable =>
            STBImage.stbi_image_free(decoded.pixels)
            promise.failure(t)
        }
      }.failed.foreach(promise.failure)
      promise.loader
    }

    private case class DecodedTexture(path: String, width: Int, height: Int, pixels: java.nio.ByteBuffer)

    private def decodeTexture(path: String): DecodedTexture = {
      val encoded = readTextureBytes(path)
      val encodedBuffer = BufferUtils.createByteBuffer(encoded.length)
      encodedBuffer.put(encoded)
      encodedBuffer.flip()

      val stack = MemoryStack.stackPush()
      try {
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        val c = stack.mallocInt(1)
        STBImage.stbi_set_flip_vertically_on_load(false)
        val pixels = STBImage.stbi_load_from_memory(encodedBuffer, w, h, c, 4)
        if(pixels == null) throw new RuntimeException("Could not load texture " + path + ": " + STBImage.stbi_failure_reason())
        DecodedTexture(path, w.get(0), h.get(0), pixels)
      } finally stack.pop()
    }

    private def uploadTexture(decoded: DecodedTexture): Texture = {
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      GL11C.glTexImage2D(Texture2D, 0, GL11C.GL_RGBA, decoded.width, decoded.height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, decoded.pixels)
      GL30C.glGenerateMipmap(Texture2D)
      texture
    }

    private def readTextureBytes(path: String): Array[Byte] = {
      val is = getClass.getClassLoader.getResourceAsStream(path)
      if(is == null) throw new ResourceNotFoundException(PartsResourcePath(path.split('/').toVector))
      try {
        val out = new ByteArrayOutputStream()
        val buffer = new Array[Byte](8192)
        var read = 0
        while({ read = is.read(buffer); read != -1 }) out.write(buffer, 0, read)
        out.toByteArray
      } finally is.close()
    }

    override def drawArrays(mode: Int, first: Int, count: Int): Unit = GL11C.glDrawArrays(mode, first, count)
    override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = GL11C.glDrawElements(mode, count, dataType, offset)
  }

  override val OpenGL: LwjglOpenGL.type = LwjglOpenGL
}
