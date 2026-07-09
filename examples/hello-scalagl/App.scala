package sgl.examples.hello.scalagl
package core

import _root_.sgl.{Application, AudioProvider, Input, InputProcessor, OpenGLProvider, PointerInputProcessor, SystemProvider}
import _root_.sgl.util.Loader

trait AbstractApp extends Application with AssetsProvider {
  this: OpenGLProvider with AudioProvider with SystemProvider =>

  private var program: Option[OpenGL.Program] = None
  private var vao: Option[OpenGL.VertexArray] = None
  private var vertexBuffer: Option[OpenGL.Buffer] = None
  private var indexBuffer: Option[OpenGL.Buffer] = None

  private var earthTextureLoader: Loader[OpenGL.Texture] = _
  private var moonTextureLoader: Loader[OpenGL.Texture] = _
  private var sunTextureLoader: Loader[OpenGL.Texture] = _
  private var ambienceLoader: Loader[Audio.Music] = _
  private var earthTexture: Option[OpenGL.Texture] = None
  private var moonTexture: Option[OpenGL.Texture] = None
  private var sunTexture: Option[OpenGL.Texture] = None
  private var ambience: Option[Audio.Music] = None

  private var mvpLocation: Option[OpenGL.UniformLocation] = None
  private var modelLocation: Option[OpenGL.UniformLocation] = None
  private var textureLocation: Option[OpenGL.UniformLocation] = None
  private var useTextureLocation: Option[OpenGL.UniformLocation] = None

  private var elapsedMillis: Long = 0L
  private var viewportWidth: Int = 1000
  private var viewportHeight: Int = 700

  private var dragging = false
  private var lastPointerX = 0
  private var lastPointerY = 0
  private var cameraYaw = 0.55f
  private var cameraPitch = -0.32f

  override def create(): Unit = {
    val vertexShaderSource = OpenGL.ShaderHeader + """

      in vec3 a_position;
      in vec3 a_normal;
      in vec3 a_color;
      in vec2 a_texcoord;

      uniform mat4 u_mvp;
      uniform mat4 u_model;

      out vec3 v_normal;
      out vec3 v_color;
      out vec2 v_texcoord;

      void main() {
        gl_Position = u_mvp * vec4(a_position, 1.0);
        v_normal = normalize(mat3(u_model) * a_normal);
        v_color = a_color;
        v_texcoord = a_texcoord;
      }
    """

    val fragmentShaderSource = OpenGL.ShaderHeader + """

      in vec3 v_normal;
      in vec3 v_color;
      in vec2 v_texcoord;

      uniform sampler2D u_texture;
      uniform float u_use_texture;

      out vec4 out_color;

      void main() {
        vec3 light_dir = normalize(vec3(-0.35, 0.5, 1.0));
        float diffuse = max(dot(normalize(v_normal), light_dir), 0.0);
        float light = 0.22 + diffuse * 0.85;
        vec3 base = mix(v_color, texture(u_texture, v_texcoord).rgb, u_use_texture);
        out_color = vec4(base * light, 1.0);
      }
    """

    val vertexShader = compileShader(OpenGL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(OpenGL.FragmentShader, fragmentShaderSource)

    val shaderProgram = OpenGL.createProgram()
    program = Some(shaderProgram)
    OpenGL.attachShader(shaderProgram, vertexShader)
    OpenGL.attachShader(shaderProgram, fragmentShader)
    OpenGL.linkProgram(shaderProgram)
    if(OpenGL.getProgramParameter(shaderProgram, OpenGL.LinkStatus) == 0) {
      throw new RuntimeException("Program link failed: " + OpenGL.getProgramInfoLog(shaderProgram))
    }
    OpenGL.deleteShader(vertexShader)
    OpenGL.deleteShader(fragmentShader)

    val cube = createCube()

    val vertexArray = OpenGL.genVertexArray()
    vao = Some(vertexArray)
    OpenGL.bindVertexArray(vertexArray)

    val vbo = OpenGL.genBuffer()
    vertexBuffer = Some(vbo)
    OpenGL.bindBuffer(OpenGL.ArrayBuffer, vbo)
    OpenGL.bufferDataFloat(OpenGL.ArrayBuffer, cube.vertices, OpenGL.StaticDraw)

    val ibo = OpenGL.genBuffer()
    indexBuffer = Some(ibo)
    OpenGL.bindBuffer(OpenGL.ElementArrayBuffer, ibo)
    OpenGL.bufferDataShort(OpenGL.ElementArrayBuffer, cube.indices, OpenGL.StaticDraw)

    val stride = 11 * 4
    val positionLocation = requireAttribute(shaderProgram, "a_position")
    OpenGL.enableVertexAttribArray(positionLocation)
    OpenGL.vertexAttribPointer(positionLocation, 3, OpenGL.FloatType, normalized = false, stride, 0L)

    val normalLocation = requireAttribute(shaderProgram, "a_normal")
    OpenGL.enableVertexAttribArray(normalLocation)
    OpenGL.vertexAttribPointer(normalLocation, 3, OpenGL.FloatType, normalized = false, stride, 3L * 4L)

    val colorLocation = requireAttribute(shaderProgram, "a_color")
    OpenGL.enableVertexAttribArray(colorLocation)
    OpenGL.vertexAttribPointer(colorLocation, 3, OpenGL.FloatType, normalized = false, stride, 6L * 4L)

    val texcoordLocation = requireAttribute(shaderProgram, "a_texcoord")
    OpenGL.enableVertexAttribArray(texcoordLocation)
    OpenGL.vertexAttribPointer(texcoordLocation, 2, OpenGL.FloatType, normalized = false, stride, 9L * 4L)

    mvpLocation = Some(requireUniform(shaderProgram, "u_mvp"))
    modelLocation = Some(requireUniform(shaderProgram, "u_model"))
    textureLocation = Some(requireUniform(shaderProgram, "u_texture"))
    useTextureLocation = Some(requireUniform(shaderProgram, "u_use_texture"))

    earthTextureLoader = OpenGL.loadTexture2D(Assets.rawImage.earth)
    moonTextureLoader = OpenGL.loadTexture2D(Assets.rawImage.moon)
    sunTextureLoader = OpenGL.loadTexture2D(Assets.rawImage.sun)
    ambienceLoader = Audio.loadMusic(Assets.audio.ambience)

    OpenGL.enable(OpenGL.DepthTest)
    OpenGL.unbindVertexArray()

    Input.setInputProcessor(new InputProcessor with PointerInputProcessor {
      override def pointerDown(x: Int, y: Int, p: Int, b: Input.MouseButtons.MouseButton): Boolean = {
        AbstractApp.this.pointerDown(x, y)
        true
      }

      override def pointerUp(x: Int, y: Int, p: Int, b: Input.MouseButtons.MouseButton): Boolean = {
        AbstractApp.this.pointerUp(x, y)
        true
      }

      override def pointerMoved(x: Int, y: Int, pointer: Int): Boolean = {
        AbstractApp.this.pointerMoved(x, y)
        true
      }
    })
  }

  override def resize(width: Int, height: Int): Unit = {
    viewportWidth = width
    viewportHeight = height
  }

  override def frame(dt: Double): Unit = {
    finishAssetLoading()
    elapsedMillis += (dt * 1000.0).toLong
    val t = elapsedMillis / 1000f

    OpenGL.clearColor(0.015f, 0.018f, 0.026f, 1.0f)
    OpenGL.clear(OpenGL.clearMask(OpenGL.ColorBufferBit, OpenGL.DepthBufferBit))

    if(program.isEmpty || vao.isEmpty || mvpLocation.isEmpty || modelLocation.isEmpty || textureLocation.isEmpty || useTextureLocation.isEmpty) return

    val aspect = viewportWidth.toFloat / scala.math.max(1, viewportHeight).toFloat
    val projection = Mat4.perspective(55f.toRadians, aspect, 0.1f, 100f)
    val cameraRotation = Mat4.multiply(Mat4.rotationX(cameraPitch), Mat4.rotationY(cameraYaw))
    val view = Mat4.multiply(Mat4.translation(0f, 0f, -4.5f), cameraRotation)
    val model = Mat4.multiply(Mat4.rotationY(t * 0.9f), Mat4.rotationX(t * 0.55f))
    val mvp = Mat4.multiply(projection, Mat4.multiply(view, model))

    OpenGL.useProgram(program.get)
    OpenGL.uniformMatrix4fv(mvpLocation.get, transpose = false, mvp)
    OpenGL.uniformMatrix4fv(modelLocation.get, transpose = false, model)
    OpenGL.uniform1i(textureLocation.get, 0)
    OpenGL.bindVertexArray(vao.get)

    drawFace(0, earthTexture)
    drawFace(1, moonTexture)
    drawFace(2, sunTexture)
    drawFace(3, None)
    drawFace(4, None)
    drawFace(5, None)

    OpenGL.unbindVertexArray()
    OpenGL.useNoProgram()
  }

  private def finishAssetLoading(): Unit = {
    if(earthTexture.isEmpty && earthTextureLoader != null && earthTextureLoader.isLoaded) earthTexture = Some(earthTextureLoader.value.get.get)
    if(moonTexture.isEmpty && moonTextureLoader != null && moonTextureLoader.isLoaded) moonTexture = Some(moonTextureLoader.value.get.get)
    if(sunTexture.isEmpty && sunTextureLoader != null && sunTextureLoader.isLoaded) sunTexture = Some(sunTextureLoader.value.get.get)
    if(ambience.isEmpty && ambienceLoader != null && ambienceLoader.isLoaded) {
      val music = ambienceLoader.value.get.get
      music.setLooping(true)
      music.setVolume(0.35f)
      music.play()
      ambience = Some(music)
    }
  }

  private def drawFace(faceIndex: Int, texture: Option[OpenGL.Texture]): Unit = {
    texture match {
      case Some(tex) =>
        OpenGL.activeTexture(OpenGL.Texture0)
        OpenGL.bindTexture(OpenGL.Texture2D, tex)
        OpenGL.uniform1f(useTextureLocation.get, 1f)
      case None =>
        OpenGL.uniform1f(useTextureLocation.get, 0f)
    }
    OpenGL.drawElements(OpenGL.Triangles, 6, OpenGL.UnsignedShortType, faceIndex * 6L * 2L)
    if(texture.nonEmpty) OpenGL.unbindTexture(OpenGL.Texture2D)
  }

  override def dispose(): Unit = {
    OpenGL.useNoProgram()
    OpenGL.unbindVertexArray()
    OpenGL.unbindBuffer(OpenGL.ArrayBuffer)
    OpenGL.unbindBuffer(OpenGL.ElementArrayBuffer)
    ambience.foreach(_.dispose())
    earthTexture.foreach(OpenGL.deleteTexture)
    moonTexture.foreach(OpenGL.deleteTexture)
    sunTexture.foreach(OpenGL.deleteTexture)
    indexBuffer.foreach(OpenGL.deleteBuffer)
    vertexBuffer.foreach(OpenGL.deleteBuffer)
    vao.foreach(OpenGL.deleteVertexArray)
    program.foreach(OpenGL.deleteProgram)
    indexBuffer = None
    vertexBuffer = None
    vao = None
    program = None
    ambience = None
    earthTexture = None
    moonTexture = None
    sunTexture = None
  }

  def pointerDown(x: Int, y: Int): Unit = {
    dragging = true
    lastPointerX = x
    lastPointerY = y
  }

  def pointerUp(x: Int, y: Int): Unit = {
    lastPointerX = x
    lastPointerY = y
    dragging = false
  }

  def pointerMoved(x: Int, y: Int): Unit = {
    if(dragging) {
      val dx = x - lastPointerX
      val dy = y - lastPointerY
      cameraYaw += dx * 0.008f
      cameraPitch += dy * 0.008f
      lastPointerX = x
      lastPointerY = y
    }
  }

  private def requireAttribute(program: OpenGL.Program, name: String): OpenGL.AttributeLocation =
    OpenGL.getAttribLocation(program, name).getOrElse(throw new RuntimeException("Missing attribute: " + name))

  private def requireUniform(program: OpenGL.Program, name: String): OpenGL.UniformLocation =
    OpenGL.getUniformLocation(program, name).getOrElse(throw new RuntimeException("Missing uniform: " + name))

  private def compileShader(shaderType: OpenGL.ShaderType, source: String): OpenGL.Shader = {
    val shader = OpenGL.createShader(shaderType)
    OpenGL.shaderSource(shader, source)
    OpenGL.compileShader(shader)
    if(OpenGL.getShaderParameter(shader, OpenGL.CompileStatus) == 0) {
      throw new RuntimeException("Shader compilation failed: " + OpenGL.getShaderInfoLog(shader))
    }
    shader
  }

  private case class Cube(vertices: Array[Float], indices: Array[Short])

  private def createCube(): Cube = {
    def face(
        p0: (Float, Float, Float), p1: (Float, Float, Float), p2: (Float, Float, Float), p3: (Float, Float, Float),
        normal: (Float, Float, Float), color: (Float, Float, Float)
    ): Array[Float] = {
      val (nx, ny, nz) = normal
      val (r, g, b) = color
      Array(
        p0._1, p0._2, p0._3, nx, ny, nz, r, g, b, 0f, 1f,
        p1._1, p1._2, p1._3, nx, ny, nz, r, g, b, 1f, 1f,
        p2._1, p2._2, p2._3, nx, ny, nz, r, g, b, 1f, 0f,
        p3._1, p3._2, p3._3, nx, ny, nz, r, g, b, 0f, 0f,
      )
    }

    val vertices = Array.concat(
      // front: earth texture
      face((-1f, -1f,  1f), ( 1f, -1f,  1f), ( 1f,  1f,  1f), (-1f,  1f,  1f), ( 0f,  0f,  1f), (1f, 1f, 1f)),
      // back: moon texture
      face(( 1f, -1f, -1f), (-1f, -1f, -1f), (-1f,  1f, -1f), ( 1f,  1f, -1f), ( 0f,  0f, -1f), (1f, 1f, 1f)),
      // left: sun texture
      face((-1f, -1f, -1f), (-1f, -1f,  1f), (-1f,  1f,  1f), (-1f,  1f, -1f), (-1f,  0f,  0f), (1f, 1f, 1f)),
      // right, top, bottom: vertex colors
      face(( 1f, -1f,  1f), ( 1f, -1f, -1f), ( 1f,  1f, -1f), ( 1f,  1f,  1f), ( 1f,  0f,  0f), (0.95f, 0.82f, 0.20f)),
      face((-1f,  1f,  1f), ( 1f,  1f,  1f), ( 1f,  1f, -1f), (-1f,  1f, -1f), ( 0f,  1f,  0f), (0.20f, 0.85f, 0.92f)),
      face((-1f, -1f, -1f), ( 1f, -1f, -1f), ( 1f, -1f,  1f), (-1f, -1f,  1f), ( 0f, -1f,  0f), (0.82f, 0.25f, 0.95f)),
    )

    val indices = Array[Short](
       0,  1,  2,   0,  2,  3,
       4,  5,  6,   4,  6,  7,
       8,  9, 10,   8, 10, 11,
      12, 13, 14,  12, 14, 15,
      16, 17, 18,  16, 18, 19,
      20, 21, 22,  20, 22, 23,
    )
    Cube(vertices, indices)
  }
}

object Mat4 {
  def translation(x: Float, y: Float, z: Float): Array[Float] = Array[Float](
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    x,  y,  z,  1f,
  )

  def rotationX(angle: Float): Array[Float] = {
    val c = scala.math.cos(angle).toFloat
    val s = scala.math.sin(angle).toFloat
    Array[Float](1f, 0f, 0f, 0f, 0f, c, s, 0f, 0f, -s, c, 0f, 0f, 0f, 0f, 1f)
  }

  def rotationY(angle: Float): Array[Float] = {
    val c = scala.math.cos(angle).toFloat
    val s = scala.math.sin(angle).toFloat
    Array[Float](c, 0f, -s, 0f, 0f, 1f, 0f, 0f, s, 0f, c, 0f, 0f, 0f, 0f, 1f)
  }

  def perspective(fovyRadians: Float, aspect: Float, near: Float, far: Float): Array[Float] = {
    val f = (1.0 / scala.math.tan(fovyRadians / 2.0)).toFloat
    val nf = 1f / (near - far)
    Array[Float](f / aspect, 0f, 0f, 0f, 0f, f, 0f, 0f, 0f, 0f, (far + near) * nf, -1f, 0f, 0f, 2f * far * near * nf, 0f)
  }

  def multiply(a: Array[Float], b: Array[Float]): Array[Float] = {
    val out = new Array[Float](16)
    var col = 0
    while(col < 4) {
      var row = 0
      while(row < 4) {
        out(col * 4 + row) = a(row) * b(col * 4) + a(4 + row) * b(col * 4 + 1) + a(8 + row) * b(col * 4 + 2) + a(12 + row) * b(col * 4 + 3)
        row += 1
      }
      col += 1
    }
    out
  }
}
