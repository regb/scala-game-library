package sgl
package html5

import org.scalajs.dom
import dom.{HTMLImageElement, WebGLRenderingContext}
import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.util.{DefaultLoader, Loader}
import dom.html

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import js.typedarray.{Float32Array, Uint16Array}

trait Html5OpenGLProvider extends OpenGLProvider {
  this: Html5SystemProvider with Html5WindowProvider =>

  protected var webglCanvas: html.Canvas = null
  protected var webgl: js.Dynamic = null

  def initOpenGL(width: Int, height: Int): Unit = {
    if(webglCanvas != null) {
      webgl = webglCanvas.getContext("webgl2").asInstanceOf[js.Dynamic]
      if(webgl == null) throw new RuntimeException("WebGL 2 is not supported by this browser")
      OpenGL.viewport(0, 0, width, height)
    }
  }

  def resizeOpenGL(width: Int, height: Int): Unit = {
    OpenGL.viewport(0, 0, width, height)
  }

  object Html5OpenGL extends OpenGL {
    type Shader = dom.WebGLShader
    type Program = dom.WebGLProgram
    type Buffer = dom.WebGLBuffer
    type VertexArray = js.Object
    type Texture = dom.WebGLTexture
    type UniformLocation = dom.WebGLUniformLocation
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

    override val ColorBufferBit: Int = WebGLRenderingContext.COLOR_BUFFER_BIT
    override val DepthBufferBit: Int = WebGLRenderingContext.DEPTH_BUFFER_BIT
    override val FloatType: Int = WebGLRenderingContext.FLOAT
    override val UnsignedShortType: Int = WebGLRenderingContext.UNSIGNED_SHORT
    override val Triangles: Int = WebGLRenderingContext.TRIANGLES
    override val DepthTest: Int = WebGLRenderingContext.DEPTH_TEST
    override val CullFace: Int = WebGLRenderingContext.CULL_FACE
    override val Back: Int = WebGLRenderingContext.BACK
    override val Blend: Int = WebGLRenderingContext.BLEND
    override val ScissorTest: Int = WebGLRenderingContext.SCISSOR_TEST
    override val SrcAlpha: Int = WebGLRenderingContext.SRC_ALPHA
    override val OneMinusSrcAlpha: Int = WebGLRenderingContext.ONE_MINUS_SRC_ALPHA
    override val VertexShader: Int = WebGLRenderingContext.VERTEX_SHADER
    override val FragmentShader: Int = WebGLRenderingContext.FRAGMENT_SHADER
    override val CompileStatus: Int = WebGLRenderingContext.COMPILE_STATUS
    override val LinkStatus: Int = WebGLRenderingContext.LINK_STATUS
    override val ArrayBuffer: Int = WebGLRenderingContext.ARRAY_BUFFER
    override val ElementArrayBuffer: Int = WebGLRenderingContext.ELEMENT_ARRAY_BUFFER
    override val StaticDraw: Int = WebGLRenderingContext.STATIC_DRAW
    override val Texture2D: Int = WebGLRenderingContext.TEXTURE_2D
    override val Texture0: Int = WebGLRenderingContext.TEXTURE0
    override val TextureMinFilter: Int = WebGLRenderingContext.TEXTURE_MIN_FILTER
    override val TextureMagFilter: Int = WebGLRenderingContext.TEXTURE_MAG_FILTER
    override val TextureWrapS: Int = WebGLRenderingContext.TEXTURE_WRAP_S
    override val TextureWrapT: Int = WebGLRenderingContext.TEXTURE_WRAP_T
    override val Linear: Int = WebGLRenderingContext.LINEAR
    override val Nearest: Int = WebGLRenderingContext.NEAREST
    override val ClampToEdge: Int = WebGLRenderingContext.CLAMP_TO_EDGE

    override def clearMask(mask: ClearMask, masks: ClearMask*): ClearMask = masks.foldLeft(mask)(_ | _)

    override def viewport(x: Int, y: Int, width: Int, height: Int): Unit = webgl.viewport(x, y, width, height)
    override def enable(capability: Int): Unit = webgl.enable(capability)
    override def disable(capability: Int): Unit = webgl.disable(capability)
    override def cullFace(mode: Int): Unit = webgl.cullFace(mode)
    override def blendFunc(source: Int, destination: Int): Unit = webgl.blendFunc(source, destination)
    override def scissor(x: Int, y: Int, width: Int, height: Int): Unit = webgl.scissor(x, y, width, height)
    override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = webgl.clearColor(red, green, blue, alpha)
    override def clear(mask: Int): Unit = webgl.clear(mask)

    override def createShader(shaderType: ShaderType): Shader = webgl.createShader(shaderType).asInstanceOf[dom.WebGLShader]
    override def shaderSource(shader: Shader, source: String): Unit = webgl.shaderSource(shader, source)
    override def compileShader(shader: Shader): Unit = webgl.compileShader(shader)
    override def getShaderParameter(shader: Shader, parameter: ShaderParameter): Int = if(webgl.getShaderParameter(shader, parameter).asInstanceOf[Boolean]) 1 else 0
    override def getShaderInfoLog(shader: Shader): String = webgl.getShaderInfoLog(shader).asInstanceOf[String]
    override def deleteShader(shader: Shader): Unit = webgl.deleteShader(shader)

    override def createProgram(): Program = webgl.createProgram().asInstanceOf[dom.WebGLProgram]
    override def attachShader(program: Program, shader: Shader): Unit = webgl.attachShader(program, shader)
    override def linkProgram(program: Program): Unit = webgl.linkProgram(program)
    override def getProgramParameter(program: Program, parameter: ProgramParameter): Int = if(webgl.getProgramParameter(program, parameter).asInstanceOf[Boolean]) 1 else 0
    override def getProgramInfoLog(program: Program): String = webgl.getProgramInfoLog(program).asInstanceOf[String]
    override def useProgram(program: Program): Unit = webgl.useProgram(program)
    override def useNoProgram(): Unit = webgl.useProgram(null)
    override def deleteProgram(program: Program): Unit = webgl.deleteProgram(program)

    override def genVertexArray(): VertexArray = webgl.createVertexArray().asInstanceOf[js.Object]
    override def bindVertexArray(vertexArray: VertexArray): Unit = webgl.bindVertexArray(vertexArray)
    override def unbindVertexArray(): Unit = webgl.bindVertexArray(null)
    override def deleteVertexArray(vertexArray: VertexArray): Unit = webgl.deleteVertexArray(vertexArray)
    override def genBuffer(): Buffer = webgl.createBuffer().asInstanceOf[dom.WebGLBuffer]
    override def bindBuffer(target: BufferTarget, buffer: Buffer): Unit = webgl.bindBuffer(target, buffer)
    override def unbindBuffer(target: BufferTarget): Unit = webgl.bindBuffer(target, null)
    override def deleteBuffer(buffer: Buffer): Unit = webgl.deleteBuffer(buffer)
    override def bufferDataFloat(target: BufferTarget, data: Array[Float], usage: BufferUsage): Unit = webgl.bufferData(target, new Float32Array(data.toJSArray), usage)
    override def bufferDataShort(target: BufferTarget, data: Array[Short], usage: BufferUsage): Unit = webgl.bufferData(target, new Uint16Array(data.map(s => s.toInt & 0xffff).toJSArray), usage)

    override def getAttribLocation(program: Program, name: String): Option[AttributeLocation] = {
      val location = webgl.getAttribLocation(program, name).asInstanceOf[Int]
      if(location >= 0) Some(location) else None
    }
    override def getUniformLocation(program: Program, name: String): Option[UniformLocation] = {
      val location = webgl.getUniformLocation(program, name).asInstanceOf[dom.WebGLUniformLocation]
      if(location == null) None else Some(location)
    }
    override def uniformMatrix4fv(location: UniformLocation, transpose: Boolean, value: Array[Float]): Unit = webgl.uniformMatrix4fv(location, transpose, new Float32Array(value.toJSArray))
    override def uniform1i(location: UniformLocation, value: Int): Unit = webgl.uniform1i(location, value)
    override def uniform1f(location: UniformLocation, value: Float): Unit = webgl.uniform1f(location, value)
    override def enableVertexAttribArray(index: AttributeLocation): Unit = webgl.enableVertexAttribArray(index)
    override def vertexAttribPointer(index: AttributeLocation, size: Int, dataType: DataType, normalized: Boolean, stride: Int, offset: Long): Unit = webgl.vertexAttribPointer(index, size, dataType, normalized, stride, offset.toDouble)

    override def genTexture(): Texture = webgl.createTexture().asInstanceOf[dom.WebGLTexture]
    override def activeTexture(texture: TextureUnit): Unit = webgl.activeTexture(texture)
    override def bindTexture(target: TextureTarget, texture: Texture): Unit = webgl.bindTexture(target, texture)
    override def unbindTexture(target: TextureTarget): Unit = webgl.bindTexture(target, null)
    override def deleteTexture(texture: Texture): Unit = webgl.deleteTexture(texture)
    override def texParameteri(target: TextureTarget, parameter: TextureParameter, value: TextureParameterValue): Unit = webgl.texParameteri(target, parameter, value)

    override def createTextureImage2D(width: Int, height: Int, rgba: Array[Byte]): TextureImage = {
      require(width > 0 && height > 0, "Texture dimensions must be positive")
      require(rgba.length == width * height * 4, "Texture data must contain tightly packed RGBA8 pixels")
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      val pixels = new js.typedarray.Uint8Array(rgba.map(value => (value.toInt & 0xff).toShort).toJSArray)
      webgl.texImage2D(Texture2D, 0, WebGLRenderingContext.RGBA, width, height, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels)
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
      val texture = genTexture()
      bindTexture(Texture2D, texture)
      texParameteri(Texture2D, TextureMinFilter, Nearest)
      texParameteri(Texture2D, TextureMagFilter, Nearest)
      texParameteri(Texture2D, TextureWrapS, ClampToEdge)
      texParameteri(Texture2D, TextureWrapT, ClampToEdge)
      val pixel = new js.typedarray.Uint8Array(js.Array[Short](255, 255, 255, 255))
      webgl.texImage2D(Texture2D, 0, WebGLRenderingContext.RGBA, 1, 1, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixel)

      val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      img.onload = (_: dom.Event) => {
        bindTexture(Texture2D, texture)
        webgl.texImage2D(Texture2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, img)
        webgl.generateMipmap(Texture2D)
        promise.success(new TextureImage(texture, img.naturalWidth, img.naturalHeight))
      }
      img.addEventListener("error", (_: dom.Event) => {
        promise.failure(new RuntimeException("Could not load texture " + html5AssetUrl(resourceName)))
      })
      img.src = html5AssetUrl(resourceName)
      promise.loader
    }

    override def drawArrays(mode: Int, first: Int, count: Int): Unit = webgl.drawArrays(mode, first, count)
    override def drawElements(mode: Int, count: Int, dataType: Int, offset: Long): Unit = webgl.drawElements(mode, count, dataType, offset.toDouble)
  }

  override val OpenGL: Html5OpenGL.type = Html5OpenGL
}
