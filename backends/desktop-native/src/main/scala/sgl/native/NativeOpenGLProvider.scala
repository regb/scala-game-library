package sgl
package native

import _root_.sgl.OpenGLProvider
import _root_.sgl.assets.{DrawableAsset, RawImageAsset}
import _root_.sgl.util.{DefaultLoader, Loader}

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.stdlib

import sdl2.SDL._
import sdl2.image.SDL_image._
import sdl2.Extras._

@extern
@link("GLESv2")
private[native] object GLES3 {
  def glViewport(x: CInt, y: CInt, width: CInt, height: CInt): Unit = extern
  def glEnable(cap: UInt): Unit = extern
  def glDisable(cap: UInt): Unit = extern
  def glCullFace(mode: UInt): Unit = extern
  def glBlendFunc(sfactor: UInt, dfactor: UInt): Unit = extern
  def glScissor(x: CInt, y: CInt, width: CInt, height: CInt): Unit = extern
  def glClearColor(red: CFloat, green: CFloat, blue: CFloat, alpha: CFloat): Unit = extern
  def glClear(mask: UInt): Unit = extern

  def glCreateShader(shaderType: UInt): UInt = extern
  def glShaderSource(shader: UInt, count: CInt, string: Ptr[CString], length: Ptr[CInt]): Unit = extern
  def glCompileShader(shader: UInt): Unit = extern
  def glGetShaderiv(shader: UInt, pname: UInt, params: Ptr[CInt]): Unit = extern
  def glGetShaderInfoLog(shader: UInt, bufSize: CInt, length: Ptr[CInt], infoLog: CString): Unit = extern
  def glDeleteShader(shader: UInt): Unit = extern

  def glCreateProgram(): UInt = extern
  def glAttachShader(program: UInt, shader: UInt): Unit = extern
  def glLinkProgram(program: UInt): Unit = extern
  def glGetProgramiv(program: UInt, pname: UInt, params: Ptr[CInt]): Unit = extern
  def glGetProgramInfoLog(program: UInt, bufSize: CInt, length: Ptr[CInt], infoLog: CString): Unit = extern
  def glUseProgram(program: UInt): Unit = extern
  def glDeleteProgram(program: UInt): Unit = extern

  def glGenVertexArrays(n: CInt, arrays: Ptr[UInt]): Unit = extern
  def glBindVertexArray(array: UInt): Unit = extern
  def glDeleteVertexArrays(n: CInt, arrays: Ptr[UInt]): Unit = extern

  def glGenBuffers(n: CInt, buffers: Ptr[UInt]): Unit = extern
  def glBindBuffer(target: UInt, buffer: UInt): Unit = extern
  def glDeleteBuffers(n: CInt, buffers: Ptr[UInt]): Unit = extern
  def glBufferData(target: UInt, size: CSize, data: Ptr[Byte], usage: UInt): Unit = extern

  def glGetAttribLocation(program: UInt, name: CString): CInt = extern
  def glGetUniformLocation(program: UInt, name: CString): CInt = extern
  def glUniformMatrix4fv(location: CInt, count: CInt, transpose: UByte, value: Ptr[CFloat]): Unit = extern
  def glUniform1i(location: CInt, value: CInt): Unit = extern
  def glUniform1f(location: CInt, value: CFloat): Unit = extern
  def glEnableVertexAttribArray(index: UInt): Unit = extern
  def glVertexAttribPointer(index: UInt, size: CInt, dataType: UInt, normalized: UByte, stride: CInt, pointerOffset: CSize): Unit = extern

  def glGenTextures(n: CInt, textures: Ptr[UInt]): Unit = extern
  def glActiveTexture(texture: UInt): Unit = extern
  def glBindTexture(target: UInt, texture: UInt): Unit = extern
  def glDeleteTextures(n: CInt, textures: Ptr[UInt]): Unit = extern
  def glTexParameteri(target: UInt, pname: UInt, param: CInt): Unit = extern
  def glTexImage2D(target: UInt, level: CInt, internalFormat: CInt, width: CInt, height: CInt, border: CInt, format: UInt, dataType: UInt, pixels: Ptr[Byte]): Unit = extern
  def glGenerateMipmap(target: UInt): Unit = extern

  def glDrawArrays(mode: UInt, first: CInt, count: CInt): Unit = extern
  def glDrawElements(mode: UInt, count: CInt, dataType: UInt, indicesOffset: CSize): Unit = extern
}

trait NativeOpenGLProvider extends OpenGLProvider {
  this: NativeSystemProvider with NativeRenderThreadDispatcher with NativeWindowProvider =>

  object NativeOpenGL extends OpenGL {
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

    override val ShaderHeader: String = "#version 300 es\nprecision mediump float;\n"

    override val ColorBufferBit: Int = 0x00004000
    override val DepthBufferBit: Int = 0x00000100

    override val FloatType: Int = 0x1406
    override val UnsignedShortType: Int = 0x1403
    override val Triangles: Int = 0x0004

    override val DepthTest: Int = 0x0B71
    override val CullFace: Int = 0x0B44
    override val Back: Int = 0x0405
    override val Blend: Int = 0x0BE2
    override val ScissorTest: Int = 0x0C11
    override val SrcAlpha: Int = 0x0302
    override val OneMinusSrcAlpha: Int = 0x0303

    override val VertexShader: Int = 0x8B31
    override val FragmentShader: Int = 0x8B30
    override val CompileStatus: Int = 0x8B81
    override val LinkStatus: Int = 0x8B82

    override val ArrayBuffer: Int = 0x8892
    override val ElementArrayBuffer: Int = 0x8893
    override val StaticDraw: Int = 0x88E4

    override val Texture2D: Int = 0x0DE1
    override val Texture0: Int = 0x84C0
    override val TextureMinFilter: Int = 0x2801
    override val TextureMagFilter: Int = 0x2800
    override val TextureWrapS: Int = 0x2802
    override val TextureWrapT: Int = 0x2803
    override val Linear: Int = 0x2601
    override val Nearest: Int = 0x2600
    override val ClampToEdge: Int = 0x812F

    override def clearMask(mask: ClearMask, masks: ClearMask*): ClearMask = masks.foldLeft(mask)(_ | _)

    override def viewport(x: Int, y: Int, width: Int, height: Int): Unit =
      GLES3.glViewport(x, y, width, height)

    override def enable(capability: Int): Unit = GLES3.glEnable(capability.toUInt)
    override def disable(capability: Int): Unit = GLES3.glDisable(capability.toUInt)
    override def cullFace(mode: Int): Unit = GLES3.glCullFace(mode.toUInt)
    override def blendFunc(source: Int, destination: Int): Unit = GLES3.glBlendFunc(source.toUInt, destination.toUInt)
    override def scissor(x: Int, y: Int, width: Int, height: Int): Unit = GLES3.glScissor(x, y, width, height)

    override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit =
      GLES3.glClearColor(red, green, blue, alpha)

    override def clear(mask: Int): Unit = GLES3.glClear(mask.toUInt)

    override def createShader(shaderType: Int): Int = GLES3.glCreateShader(shaderType.toUInt).toInt

    override def shaderSource(shader: Int, source: String): Unit = Zone.acquire { implicit z =>
      val src = toCString(source)
      val strings = stackalloc[CString]()
      !strings = src
      GLES3.glShaderSource(shader.toUInt, 1, strings, null)
    }

    override def compileShader(shader: Int): Unit = GLES3.glCompileShader(shader.toUInt)

    override def getShaderParameter(shader: Int, parameter: Int): Int = {
      val value = stackalloc[CInt]()
      GLES3.glGetShaderiv(shader.toUInt, parameter.toUInt, value)
      !value
    }

    override def getShaderInfoLog(shader: Int): String = {
      val bufferSize = 4096
      val buffer = stdlib.malloc(bufferSize.toUInt).asInstanceOf[CString]
      val length = stackalloc[CInt]()
      GLES3.glGetShaderInfoLog(shader.toUInt, bufferSize, length, buffer)
      val log = fromCString(buffer)
      stdlib.free(buffer.asInstanceOf[Ptr[Byte]])
      log
    }

    override def deleteShader(shader: Int): Unit = GLES3.glDeleteShader(shader.toUInt)

    override def createProgram(): Int = GLES3.glCreateProgram().toInt
    override def attachShader(program: Int, shader: Int): Unit = GLES3.glAttachShader(program.toUInt, shader.toUInt)
    override def linkProgram(program: Int): Unit = GLES3.glLinkProgram(program.toUInt)

    override def getProgramParameter(program: Int, parameter: Int): Int = {
      val value = stackalloc[CInt]()
      GLES3.glGetProgramiv(program.toUInt, parameter.toUInt, value)
      !value
    }

    override def getProgramInfoLog(program: Int): String = {
      val bufferSize = 4096
      val buffer = stdlib.malloc(bufferSize.toUInt).asInstanceOf[CString]
      val length = stackalloc[CInt]()
      GLES3.glGetProgramInfoLog(program.toUInt, bufferSize, length, buffer)
      val log = fromCString(buffer)
      stdlib.free(buffer.asInstanceOf[Ptr[Byte]])
      log
    }

    override def useProgram(program: Program): Unit = GLES3.glUseProgram(program.toUInt)
    override def useNoProgram(): Unit = GLES3.glUseProgram(0.toUInt)
    override def deleteProgram(program: Program): Unit = GLES3.glDeleteProgram(program.toUInt)

    override def genVertexArray(): VertexArray = {
      val vao = stackalloc[UInt]()
      GLES3.glGenVertexArrays(1, vao)
      (!vao).toInt
    }
    override def bindVertexArray(vertexArray: VertexArray): Unit = GLES3.glBindVertexArray(vertexArray.toUInt)
    override def unbindVertexArray(): Unit = GLES3.glBindVertexArray(0.toUInt)
    override def deleteVertexArray(vertexArray: VertexArray): Unit = {
      val vao = stackalloc[UInt]()
      !vao = vertexArray.toUInt
      GLES3.glDeleteVertexArrays(1, vao)
    }

    override def genBuffer(): Buffer = {
      val buffer = stackalloc[UInt]()
      GLES3.glGenBuffers(1, buffer)
      (!buffer).toInt
    }
    override def bindBuffer(target: BufferTarget, buffer: Buffer): Unit = GLES3.glBindBuffer(target.toUInt, buffer.toUInt)
    override def unbindBuffer(target: BufferTarget): Unit = GLES3.glBindBuffer(target.toUInt, 0.toUInt)
    override def deleteBuffer(buffer: Buffer): Unit = {
      val b = stackalloc[UInt]()
      !b = buffer.toUInt
      GLES3.glDeleteBuffers(1, b)
    }

    override def bufferDataFloat(target: BufferTarget, data: Array[Float], usage: BufferUsage): Unit = {
      val bytes: CSize = (data.length * 4).toUInt
      val nativeData = stdlib.malloc(bytes).asInstanceOf[Ptr[CFloat]]
      var i = 0
      while(i < data.length) {
        nativeData(i) = data(i)
        i += 1
      }
      GLES3.glBufferData(target.toUInt, bytes, nativeData.asInstanceOf[Ptr[Byte]], usage.toUInt)
      stdlib.free(nativeData.asInstanceOf[Ptr[Byte]])
    }

    override def bufferDataShort(target: BufferTarget, data: Array[Short], usage: BufferUsage): Unit = {
      val bytes: CSize = (data.length * 2).toUInt
      val nativeData = stdlib.malloc(bytes).asInstanceOf[Ptr[CShort]]
      var i = 0
      while(i < data.length) {
        nativeData(i) = data(i)
        i += 1
      }
      GLES3.glBufferData(target.toUInt, bytes, nativeData.asInstanceOf[Ptr[Byte]], usage.toUInt)
      stdlib.free(nativeData.asInstanceOf[Ptr[Byte]])
    }

    override def getAttribLocation(program: Program, name: String): Option[AttributeLocation] = Zone.acquire { implicit z =>
      val location = GLES3.glGetAttribLocation(program.toUInt, toCString(name))
      if(location >= 0) Some(location) else None
    }

    override def getUniformLocation(program: Program, name: String): Option[UniformLocation] = Zone.acquire { implicit z =>
      val location = GLES3.glGetUniformLocation(program.toUInt, toCString(name))
      if(location >= 0) Some(location) else None
    }

    override def uniformMatrix4fv(location: Int, transpose: Boolean, value: Array[Float]): Unit = {
      val nativeData = stackalloc[CFloat](value.length.toUInt)
      var i = 0
      while(i < value.length) {
        nativeData(i) = value(i)
        i += 1
      }
      GLES3.glUniformMatrix4fv(location, 1, if(transpose) 1.toUByte else 0.toUByte, nativeData)
    }

    override def uniform1i(location: Int, value: Int): Unit = GLES3.glUniform1i(location, value)
    override def uniform1f(location: Int, value: Float): Unit = GLES3.glUniform1f(location, value)

    override def enableVertexAttribArray(index: Int): Unit = GLES3.glEnableVertexAttribArray(index.toUInt)

    override def vertexAttribPointer(index: Int, size: Int, dataType: Int, normalized: Boolean, stride: Int, offset: Long): Unit =
      GLES3.glVertexAttribPointer(index.toUInt, size, dataType.toUInt, if(normalized) 1.toUByte else 0.toUByte, stride, offset.toUInt)

    override def genTexture(): Texture = {
      val texture = stackalloc[UInt]()
      GLES3.glGenTextures(1, texture)
      (!texture).toInt
    }
    override def activeTexture(texture: TextureUnit): Unit = GLES3.glActiveTexture(texture.toUInt)
    override def bindTexture(target: TextureTarget, texture: Texture): Unit = GLES3.glBindTexture(target.toUInt, texture.toUInt)
    override def unbindTexture(target: TextureTarget): Unit = GLES3.glBindTexture(target.toUInt, 0.toUInt)
    override def deleteTexture(texture: Texture): Unit = {
      val t = stackalloc[UInt]()
      !t = texture.toUInt
      GLES3.glDeleteTextures(1, t)
    }
    override def texParameteri(target: TextureTarget, parameter: TextureParameter, value: TextureParameterValue): Unit = GLES3.glTexParameteri(target.toUInt, parameter.toUInt, value)

    override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = {
      require(width > 0 && height > 0, "Texture dimensions must be positive")
      require(rgba.length == width * height * 4, "Texture data must contain tightly packed RGBA8 pixels")
      val nativePixels = stdlib.malloc(rgba.length.toUInt).asInstanceOf[Ptr[Byte]]
      var index = 0
      while(index < rgba.length) {
        nativePixels(index) = rgba(index)
        index += 1
      }
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      GLES3.glTexImage2D(Texture2D.toUInt, 0, 0x1908, width, height, 0, 0x1908.toUInt, 0x1401.toUInt, nativePixels)
      stdlib.free(nativePixels)
      new TextureImage(texture, width, height)
    }

    override def loadTextureImage2D(asset: DrawableAsset): Loader[TextureImage] = {
      val variant = asset.bestVariantForDpi(Window.logicalPpi)
      loadTexture2DResource(variant.resourceName)
    }

    override def loadTextureImage2D(asset: RawImageAsset): Loader[TextureImage] =
      loadTexture2DResource(asset.resourceName)

    private def loadTexture2DResource(resourceName: String): Loader[TextureImage] = {
      val promise = new DefaultLoader[TextureImage]
      val thread = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            val surface = loadTextureSurface(resourceName)
            try {
              runOnOpenGLThread {
                try {
                  val texture = uploadTextureSurface(surface)
                  promise.success(new TextureImage(texture, surface.w, surface.h))
                } catch {
                  case t: Throwable => promise.failure(t)
                } finally {
                  SDL_FreeSurface(surface)
                }
              }
            } catch {
              case t: Throwable =>
                SDL_FreeSurface(surface)
                promise.failure(t)
            }
          } catch {
            case t: Throwable => promise.failure(t)
          }
        }
      })
      thread.setDaemon(true)
      thread.start()
      promise.loader
    }

    private def loadTextureSurface(resourceName: String): Ptr[SDL_Surface] = Zone.acquire { implicit z =>
      val path = nativeAssetPath(resourceName)
      val surface = IMG_Load(toCString(path))
      if(surface == null) throw new RuntimeException("Could not load texture " + path + ": " + fromCString(SDL_GetError()))
      surface
    }

    private def uploadTextureSurface(surface: Ptr[SDL_Surface]): Texture = {
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      val format = if(surface.format.BytesPerPixel == 4.toUByte) 0x1908 else 0x1907
      GLES3.glTexImage2D(Texture2D.toUInt, 0, format, surface.w, surface.h, 0, format.toUInt, 0x1401.toUInt, surface.pixels)
      GLES3.glGenerateMipmap(Texture2D.toUInt)
      texture
    }

    override def drawArrays(mode: Int, first: Int, count: Int): Unit = GLES3.glDrawArrays(mode.toUInt, first, count)
    override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = GLES3.glDrawElements(mode.toUInt, count, dataType.toUInt, offset.toUInt)
  }

  override val OpenGL: NativeOpenGL.type = NativeOpenGL
}
