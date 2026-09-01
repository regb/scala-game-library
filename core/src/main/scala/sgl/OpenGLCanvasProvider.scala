package sgl

import sgl.assets.{DrawableAsset, FontAsset}
import sgl.util.Loader

import scala.collection.mutable.ArrayBuffer

/** Implements the Canvas API with the portable [[OpenGLProvider]] API.
  *
  * The renderer uses top-left pixel coordinates, CPU-side affine transforms,
  * shader-based solid and textured triangles, and scissor rectangles. It can
  * therefore run unchanged on desktop OpenGL, OpenGL ES, WebGL 2, and the
  * surfaceless headless backend.
  */
trait OpenGLCanvasProvider extends CanvasProvider with Application {
  this: OpenGLProvider with WindowProvider with SystemProvider =>

  object OpenGLCanvasGraphics extends Graphics {
    final class OpenGLBitmap(private[OpenGLCanvasProvider] val image: OpenGL.TextureImage) extends AbstractBitmap {
      private var released = false

      override val width: Int = image.width
      override val height: Int = image.height

      private[OpenGLCanvasProvider] def texture: OpenGL.Texture = {
        if(released) throw new IllegalStateException("Trying to use a released bitmap")
        image.texture
      }

      override def release(): Unit = if(!released) {
        released = true
        runOnOpenGLThread(OpenGL.deleteTexture(image.texture))
      }
    }
    override type Bitmap = OpenGLBitmap

    override def loadImage(asset: DrawableAsset): Loader[Bitmap] =
      OpenGL.loadTextureImage2D(asset).map(new OpenGLBitmap(_))

    final case class OpenGLFont(family: String, style: Font.Style, size: Int) extends AbstractFont {
      require(size > 0, "Font size must be positive")
      override def withSize(newSize: Int): Font = copy(size = newSize)
      override def withStyle(newStyle: Font.Style): Font = copy(style = newStyle)
      override def isBold: Boolean = style == Font.Bold || style == Font.BoldItalic
      override def isItalic: Boolean = style == Font.Italic || style == Font.BoldItalic
    }
    override type Font = OpenGLFont

    object OpenGLFontCompanion extends FontCompanion {
      override def create(family: String, style: Style, size: Int): Font = OpenGLFont(family, style, size)
      override def load(asset: FontAsset): Loader[Font] = Loader.successful(Default)
      override val Default: Font = OpenGLFont("Noto Sans", Normal, 14)
      override val DefaultBold: Font = OpenGLFont("Noto Sans", Bold, 14)
      override val Monospace: Font = OpenGLFont("Noto Sans", Normal, 14)
      override val SansSerif: Font = OpenGLFont("Noto Sans", Normal, 14)
      override val Serif: Font = OpenGLFont("Noto Sans", Normal, 14)
    }
    override val Font: OpenGLFontCompanion.type = OpenGLFontCompanion

    final case class OpenGLColor(red: Int, green: Int, blue: Int, alpha: Int)
    override type Color = OpenGLColor

    object OpenGLColorCompanion extends ColorCompanion {
      private def channel(value: Int): Int = {
        require(value >= 0 && value <= 255, "Color channels must be between 0 and 255")
        value
      }
      override def rgb(red: Int, green: Int, blue: Int): Color =
        OpenGLColor(channel(red), channel(green), channel(blue), 255)
      override def rgba(red: Int, green: Int, blue: Int, alpha: Int): Color =
        OpenGLColor(channel(red), channel(green), channel(blue), channel(alpha))
    }
    override val Color: OpenGLColorCompanion.type = OpenGLColorCompanion

    final case class OpenGLPaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
      override def withFont(newFont: Font): Paint = copy(font = newFont)
      override def withColor(newColor: Color): Paint = copy(color = newColor)
      override def withAlignment(newAlignment: Alignments.Alignment): Paint = copy(alignment = newAlignment)
    }
    override type Paint = OpenGLPaint
    override val defaultPaint: Paint = OpenGLPaint(Font.Default, Color.Black, Alignments.Left)

    final class OpenGLTextLayout(
        override val lines: Vector[String],
        override val layoutWidth: Int,
        val paint: Paint,
        override val overflowed: Boolean
    ) extends AbstractTextLayout {
      override val lineCount: Int = lines.length
      private val face = PortableFont.face(paint.font.isBold)
      private val fontScale = PortableFont.scale(paint.font.size)
      override val lineHeight: Int = scala.math.ceil(face.lineHeight * fontScale).toInt
      override val ascent: Int = scala.math.ceil(face.ascent * fontScale).toInt
      override val descent: Int = scala.math.ceil(face.descent * fontScale).toInt
      override val width: Int = scala.math.ceil(lines.foldLeft(0f)((result, line) => scala.math.max(result, measure(line, paint.font)))).toInt
      override val height: Int = if(lineCount == 0) 0 else ascent + descent + (lineCount - 1) * lineHeight
    }
    override type TextLayout = OpenGLTextLayout

    final class OpenGLCanvas extends AbstractCanvas {
      private var transform = Transform.Identity
      private var clip: Option[Clip] = None

      private[OpenGLCanvasProvider] def reset(): Unit = {
        transform = Transform.Identity
        clip = None
      }

      override def withSave[A](body: => A): A = {
        val savedTransform = transform
        val savedClip = clip
        try body
        finally {
          flush()
          transform = savedTransform
          clip = savedClip
          applyClip(savedClip)
        }
      }

      override def translate(x: Float, y: Float): Unit = transform = transform.translate(x, y)
      override def rotate(theta: Float): Unit = transform = transform.rotate(theta)
      override def scale(x: Float, y: Float): Unit = transform = transform.scale(x, y)

      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = {
        val corners = Array(
          transform(x, y),
          transform(x + width, y),
          transform(x + width, y + height),
          transform(x, y + height)
        )
        val requested = Clip(
          corners.map(_._1).min,
          corners.map(_._2).min,
          corners.map(_._1).max,
          corners.map(_._2).max
        )
        flush()
        clip = Some(clip.fold(requested)(_.intersect(requested)))
        applyClip(clip)
      }

      override def drawBitmap(
          bitmap: Bitmap,
          destinationX: Float,
          destinationY: Float,
          destinationWidth: Float,
          destinationHeight: Float,
          sourceX: Int,
          sourceY: Int,
          sourceWidth: Int,
          sourceHeight: Int,
          alpha: Float
      ): Unit = {
        require(alpha >= 0f && alpha <= 1f, "Bitmap alpha must be between 0 and 1")
        require(sourceWidth >= 0 && sourceHeight >= 0, "Bitmap source dimensions must not be negative")
        val u0 = sourceX.toFloat / bitmap.width
        val v0 = sourceY.toFloat / bitmap.height
        val u1 = (sourceX + sourceWidth).toFloat / bitmap.width
        val v1 = (sourceY + sourceHeight).toFloat / bitmap.height
        useTexture(Some(bitmap.texture))
        addQuad(destinationX, destinationY, destinationWidth, destinationHeight, u0, v0, u1, v1,
          OpenGLColor(255, 255, 255, scala.math.round(alpha * 255f)))
      }

      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        useTexture(None)
        addQuad(x, y, width, height, 0f, 0f, 0f, 0f, paint.color)
      }

      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        useTexture(None)
        val segments = 40
        var segment = 0
        while(segment < segments) {
          val angle0 = segment.toDouble * scala.math.Pi * 2.0 / segments
          val angle1 = (segment + 1).toDouble * scala.math.Pi * 2.0 / segments
          addTriangle(
            (x, y, 0f, 0f),
            (x + scala.math.cos(angle0).toFloat * width / 2f, y + scala.math.sin(angle0).toFloat * height / 2f, 0f, 0f),
            (x + scala.math.cos(angle1).toFloat * width / 2f, y + scala.math.sin(angle1).toFloat * height / 2f, 0f, 0f),
            paint.color
          )
          segment += 1
        }
      }

      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = {
        val dx = x2 - x1
        val dy = y2 - y1
        val length = scala.math.sqrt(dx * dx + dy * dy).toFloat
        if(length > 0f) {
          val nx = -dy / length * 0.5f
          val ny = dx / length * 0.5f
          useTexture(None)
          addTriangle((x1 + nx, y1 + ny, 0f, 0f), (x1 - nx, y1 - ny, 0f, 0f), (x2 - nx, y2 - ny, 0f, 0f), paint.color)
          addTriangle((x1 + nx, y1 + ny, 0f, 0f), (x2 - nx, y2 - ny, 0f, 0f), (x2 + nx, y2 + ny, 0f, 0f), paint.color)
        }
      }

      override def drawString(value: String, x: Float, baselineY: Float, paint: Paint): Unit = {
        val startX = paint.alignment match {
          case Alignments.Left => x
          case Alignments.Center => x - measure(value, paint.font) / 2f
          case Alignments.Right => x - measure(value, paint.font)
        }
        drawGlyphs(value, startX, baselineY, paint)
      }

      override def drawText(text: TextLayout, x: Float, y: Float): Unit = {
        var lineIndex = 0
        while(lineIndex < text.lines.length) {
          val line = text.lines(lineIndex)
          val lineX = text.paint.alignment match {
            case Alignments.Left => x
            case Alignments.Center => x + (text.layoutWidth - measure(line, text.paint.font)) / 2f
            case Alignments.Right => x + text.layoutWidth - measure(line, text.paint.font)
          }
          drawGlyphs(line, lineX, y + text.ascent + lineIndex * text.lineHeight, text.paint.copy(alignment = Alignments.Left))
          lineIndex += 1
        }
      }

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        require(width > 0, "Text layout width must be positive")
        val wrapped = TextWrapping.wrap(text, width, value => measure(value, paint.font))
        new OpenGLTextLayout(wrapped.lines, width, paint, wrapped.overflowed)
      }

      private def drawGlyphs(value: String, x: Float, baselineY: Float, paint: Paint): Unit = {
        val face = PortableFont.face(paint.font.isBold)
        val scale = PortableFont.scale(paint.font.size)
        useTexture(Some(fontTexture(face, paint.font.isBold)))
        var cursorX = x
        PortableFont.foreachCodePoint(value) { codePoint =>
          val glyph = face.glyph(codePoint)
          if(glyph.width > 0 && glyph.height > 0) {
            val u0 = glyph.x.toFloat / face.atlasWidth
            val v0 = glyph.y.toFloat / face.atlasHeight
            val u1 = (glyph.x + glyph.width).toFloat / face.atlasWidth
            val v1 = (glyph.y + glyph.height).toFloat / face.atlasHeight
            addQuad(
              cursorX + glyph.bearingX * scale,
              baselineY + glyph.bearingY * scale,
              glyph.width * scale,
              glyph.height * scale,
              u0, v0, u1, v1,
              paint.color
            )
          }
          cursorX += glyph.advance * scale
        }
      }

      private def addQuad(
          x: Float,
          y: Float,
          width: Float,
          height: Float,
          u0: Float,
          v0: Float,
          u1: Float,
          v1: Float,
          color: Color
      ): Unit = {
        addTriangle((x, y, u0, v0), (x, y + height, u0, v1), (x + width, y + height, u1, v1), color)
        addTriangle((x, y, u0, v0), (x + width, y + height, u1, v1), (x + width, y, u1, v0), color)
      }

      private def addTriangle(
          first: (Float, Float, Float, Float),
          second: (Float, Float, Float, Float),
          third: (Float, Float, Float, Float),
          color: Color
      ): Unit = {
        addVertex(first, color)
        addVertex(second, color)
        addVertex(third, color)
        if(vertices.length >= MaxBatchFloats) flush()
      }

      private def addVertex(vertex: (Float, Float, Float, Float), color: Color): Unit = {
        val point = transform(vertex._1, vertex._2)
        vertices += point._1 / frameWidth * 2f - 1f
        vertices += 1f - point._2 / frameHeight * 2f
        vertices += vertex._3
        vertices += vertex._4
        vertices += color.red / 255f
        vertices += color.green / 255f
        vertices += color.blue / 255f
        vertices += color.alpha / 255f
        vertices += (if(batchTexture.isDefined) 1f else 0f)
      }
    }
    override type Canvas = OpenGLCanvas
    private[OpenGLCanvasProvider] val canvas = new OpenGLCanvas

    private def measure(value: String, font: Font): Float =
      PortableFont.measure(value, PortableFont.face(font.isBold), font.size)
  }
  override val Graphics: OpenGLCanvasGraphics.type = OpenGLCanvasGraphics

  private val VertexFloats = 9
  private val MaxBatchFloats = VertexFloats * 6 * 256
  private val vertices = ArrayBuffer.empty[Float]
  private var batchTexture: Option[OpenGL.Texture] = None
  private var frameWidth = 1f
  private var frameHeight = 1f
  private var active = false
  private var initialized = false
  private var program: OpenGL.Program = _
  private var vertexArray: OpenGL.VertexArray = _
  private var vertexBuffer: OpenGL.Buffer = _
  private var whiteTexture: OpenGL.Texture = _
  private var regularFontTexture: Option[OpenGL.Texture] = None
  private var boldFontTexture: Option[OpenGL.Texture] = None

  final override def withFrameCanvas[A](f: Graphics.Canvas => A): A = {
    if(active) throw new IllegalStateException("Canvas frame is already active")
    initializeRenderer()
    active = true
    frameWidth = Window.width.toFloat
    frameHeight = Window.height.toFloat
    vertices.clear()
    batchTexture = None
    Graphics.canvas.reset()

    OpenGL.viewport(0, 0, Window.width, Window.height)
    OpenGL.disable(OpenGL.DepthTest)
    OpenGL.disable(OpenGL.CullFace)
    OpenGL.disable(OpenGL.ScissorTest)
    OpenGL.enable(OpenGL.Blend)
    OpenGL.blendFunc(OpenGL.SrcAlpha, OpenGL.OneMinusSrcAlpha)
    OpenGL.useProgram(program)
    OpenGL.bindVertexArray(vertexArray)

    try f(Graphics.canvas)
    finally {
      try flush()
      finally {
        OpenGL.disable(OpenGL.ScissorTest)
        OpenGL.unbindTexture(OpenGL.Texture2D)
        OpenGL.unbindVertexArray()
        OpenGL.useNoProgram()
        active = false
      }
    }
  }

  abstract override def dispose(): Unit = {
    try super.dispose()
    finally disposeOpenGLCanvas()
  }

  /** Releases shader and buffer resources owned by this Canvas renderer. */
  final def disposeOpenGLCanvas(): Unit = if(initialized) {
    OpenGL.deleteBuffer(vertexBuffer)
    OpenGL.deleteVertexArray(vertexArray)
    OpenGL.deleteTexture(whiteTexture)
    regularFontTexture.foreach(OpenGL.deleteTexture)
    boldFontTexture.foreach(OpenGL.deleteTexture)
    regularFontTexture = None
    boldFontTexture = None
    OpenGL.deleteProgram(program)
    initialized = false
  }

  private def initializeRenderer(): Unit = if(!initialized) {
    val vertexShader = compileShader(OpenGL.VertexShader, OpenGL.ShaderHeader + """
      in vec2 a_position;
      in vec2 a_texcoord;
      in vec4 a_color;
      in float a_textured;
      out vec2 v_texcoord;
      out vec4 v_color;
      out float v_textured;
      void main() {
        gl_Position = vec4(a_position, 0.0, 1.0);
        v_texcoord = a_texcoord;
        v_color = a_color;
        v_textured = a_textured;
      }
    """)
    val fragmentShader = compileShader(OpenGL.FragmentShader, OpenGL.ShaderHeader + """
      in vec2 v_texcoord;
      in vec4 v_color;
      in float v_textured;
      uniform sampler2D u_texture;
      out vec4 out_color;
      void main() {
        vec4 sampled = texture(u_texture, v_texcoord);
        out_color = v_color * mix(vec4(1.0), sampled, step(0.5, v_textured));
      }
    """)

    program = OpenGL.createProgram()
    OpenGL.attachShader(program, vertexShader)
    OpenGL.attachShader(program, fragmentShader)
    OpenGL.linkProgram(program)
    if(OpenGL.getProgramParameter(program, OpenGL.LinkStatus) == 0)
      throw new RuntimeException("OpenGL Canvas program link failed: " + OpenGL.getProgramInfoLog(program))
    OpenGL.deleteShader(vertexShader)
    OpenGL.deleteShader(fragmentShader)

    whiteTexture = OpenGL.createTextureImage2D(1, 1, Array[Byte](-1, -1, -1, -1)).texture
    vertexArray = OpenGL.genVertexArray()
    vertexBuffer = OpenGL.genBuffer()
    OpenGL.bindVertexArray(vertexArray)
    OpenGL.bindBuffer(OpenGL.ArrayBuffer, vertexBuffer)
    val stride = VertexFloats * 4
    configureAttribute("a_position", 2, stride, 0L)
    configureAttribute("a_texcoord", 2, stride, 2L * 4L)
    configureAttribute("a_color", 4, stride, 4L * 4L)
    configureAttribute("a_textured", 1, stride, 8L * 4L)
    OpenGL.getUniformLocation(program, "u_texture").foreach { location =>
      OpenGL.useProgram(program)
      OpenGL.uniform1i(location, 0)
      OpenGL.useNoProgram()
    }
    OpenGL.unbindBuffer(OpenGL.ArrayBuffer)
    OpenGL.unbindVertexArray()
    initialized = true
  }

  private def compileShader(shaderType: OpenGL.ShaderType, source: String): OpenGL.Shader = {
    val shader = OpenGL.createShader(shaderType)
    OpenGL.shaderSource(shader, source)
    OpenGL.compileShader(shader)
    if(OpenGL.getShaderParameter(shader, OpenGL.CompileStatus) == 0) {
      val log = OpenGL.getShaderInfoLog(shader)
      OpenGL.deleteShader(shader)
      throw new RuntimeException("OpenGL Canvas shader compilation failed: " + log)
    }
    shader
  }

  private def configureAttribute(name: String, size: Int, stride: Int, offset: Long): Unit = {
    val location = OpenGL.getAttribLocation(program, name).getOrElse(
      throw new IllegalStateException("OpenGL Canvas attribute is missing: " + name)
    )
    OpenGL.enableVertexAttribArray(location)
    OpenGL.vertexAttribPointer(location, size, OpenGL.FloatType, normalized = false, stride, offset)
  }

  private def fontTexture(face: PortableFont.Face, bold: Boolean): OpenGL.Texture = {
    val current = if(bold) boldFontTexture else regularFontTexture
    current.getOrElse {
      val texture = OpenGL.createTextureImage2D(face.atlasWidth, face.atlasHeight, face.rgba).texture
      OpenGL.bindTexture(OpenGL.Texture2D, texture)
      OpenGL.texParameteri(OpenGL.Texture2D, OpenGL.TextureMinFilter, OpenGL.Linear)
      OpenGL.texParameteri(OpenGL.Texture2D, OpenGL.TextureMagFilter, OpenGL.Linear)
      if(bold) boldFontTexture = Some(texture) else regularFontTexture = Some(texture)
      texture
    }
  }

  private def useTexture(texture: Option[OpenGL.Texture]): Unit = {
    if(texture != batchTexture) {
      flush()
      batchTexture = texture
    }
  }

  private def flush(): Unit = if(vertices.nonEmpty) {
    OpenGL.bindVertexArray(vertexArray)
    OpenGL.bindBuffer(OpenGL.ArrayBuffer, vertexBuffer)
    OpenGL.bufferDataFloat(OpenGL.ArrayBuffer, vertices.toArray, OpenGL.StaticDraw)
    batchTexture match {
      case Some(texture) =>
        OpenGL.activeTexture(OpenGL.Texture0)
        OpenGL.bindTexture(OpenGL.Texture2D, texture)
      case None => OpenGL.bindTexture(OpenGL.Texture2D, whiteTexture)
    }
    OpenGL.drawArrays(OpenGL.Triangles, 0, vertices.length / VertexFloats)
    vertices.clear()
  }

  private def applyClip(clip: Option[Clip]): Unit = clip match {
    case Some(value) =>
      val left = scala.math.max(0, scala.math.floor(value.left).toInt)
      val top = scala.math.max(0, scala.math.floor(value.top).toInt)
      val right = scala.math.min(Window.width, scala.math.ceil(value.right).toInt)
      val bottom = scala.math.min(Window.height, scala.math.ceil(value.bottom).toInt)
      OpenGL.enable(OpenGL.ScissorTest)
      OpenGL.scissor(left, Window.height - bottom, scala.math.max(0, right - left), scala.math.max(0, bottom - top))
    case None => OpenGL.disable(OpenGL.ScissorTest)
  }

  private final case class Clip(left: Float, top: Float, right: Float, bottom: Float) {
    def intersect(other: Clip): Clip = Clip(
      scala.math.max(left, other.left),
      scala.math.max(top, other.top),
      scala.math.min(right, other.right),
      scala.math.min(bottom, other.bottom)
    )
  }

  private final case class Transform(a: Float, b: Float, c: Float, d: Float, tx: Float, ty: Float) {
    def apply(x: Float, y: Float): (Float, Float) = (a * x + c * y + tx, b * x + d * y + ty)
    def translate(x: Float, y: Float): Transform = copy(tx = a * x + c * y + tx, ty = b * x + d * y + ty)
    def scale(x: Float, y: Float): Transform = Transform(a * x, b * x, c * y, d * y, tx, ty)
    def rotate(theta: Float): Transform = {
      val cosine = scala.math.cos(theta).toFloat
      val sine = scala.math.sin(theta).toFloat
      Transform(a * cosine + c * sine, b * cosine + d * sine, c * cosine - a * sine, d * cosine - b * sine, tx, ty)
    }
  }
  private object Transform {
    val Identity: Transform = Transform(1f, 0f, 0f, 1f, 0f, 0f)
  }

}
