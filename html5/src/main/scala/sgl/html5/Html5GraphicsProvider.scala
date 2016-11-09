package sgl
package html5

import org.scalajs.dom
import dom.html
import dom.raw.HTMLImageElement

trait Html5GraphicsProvider extends GraphicsProvider with Lifecycle {
  this: Html5WindowProvider =>

  abstract override def startup(): Unit = {
    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
  }


  //could wrap that into a LoaderProxy, that makes sure image is loaded before drawing it
  case class Html5Bitmap(image: HTMLImageElement) extends AbstractBitmap {
    override def height: Int = image.height
    override def width: Int = image.width
  }
  type Bitmap = Html5Bitmap

  override def loadImageFromResource(path: String): Bitmap = {
    val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    img.onload = (e: dom.Event) => {
      println("image loaded")
    }
    img.src = s"static/$path"
    Html5Bitmap(img)
  }


  case class Html5Font(family: String, style: Font.Style, size: Int) extends AbstractFont {
    override def withSize(s: Int): Font = copy(size = s)
    override def withStyle(s: Font.Style): Font = copy(style = s)

    override def isBold: Boolean = style == Font.Bold || style == Font.BoldItalic
    override def isItalic: Boolean = style == Font.Italic || style == Font.BoldItalic

    def asCss: String = {
      //not sure, but seems that the ordering of size/style/family is important.
      //"20px normal sans-serif" seems to be the most reasonable
      val scss = Font.toCssStyle(style)
      s"${size}px $scss $family"
    }
  }
  type Font = Html5Font

  object Html5FontCompanion extends FontCompanion {

    override def create(family: String, style: Style, size: Int): Font = Html5Font(family, style, size)


    def toCssStyle(s: Font.Style): String = s match {
      case Bold => "bold"
      case Italic => "italic"
      case Normal => "normal"
      case BoldItalic => "bold italic"
    }

    override lazy val Default: Font = Html5Font("sans-serif", Normal, 10)
    override lazy val DefaultBold: Font = Html5Font("sans-serif", Bold, 10)
    override lazy val Monospace: Font = Html5Font("monospace", Normal, 10)
    override lazy val SansSerif: Font = Html5Font("sans-serif", Normal, 10)
    override lazy val Serif: Font = Html5Font("serif", Normal, 10)
  }
  override val Font = Html5FontCompanion

  type Color = String
  object Html5ColorCompanion extends ColorCompanion {
    override def rgb(r: Int, g: Int, b: Int): Color = s"rgb($r,$g,$b)"
    override def rgba(r: Int, g: Int, b: Int, a: Int): Color = s"rgba($r,$g,$b)"
  }
  override val Color = Html5ColorCompanion

  case class Html5Paint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
    val alignmentRaw = alignment match {
      case Alignments.Left => "left"
      case Alignments.Center => "center"
      case Alignments.Right => "right"
    }
    def withFont(f: Font) = copy(font = f)
    def withColor(c: Color) = copy(color = c)
    def withAlignment(a: Alignments.Alignment) = copy(alignment = a)

    def prepareContext(ctx: Ctx2D): Unit = {
      ctx.fillStyle = color
      ctx.strokeStyle = color
      ctx.font = font.asCss
      ctx.textAlign = alignmentRaw
    }
  }

  type Paint = Html5Paint
  override def defaultPaint: Paint = Html5Paint(Font.Default, Color.Black, Alignments.Left)

  type Ctx2D = dom.CanvasRenderingContext2D

  case class Html5Canvas(canvas: html.Canvas) extends AbstractCanvas {
    
    val context = canvas.getContext("2d").asInstanceOf[Ctx2D]
    context.save()

    override def height: Int = canvas.height
    override def width: Int = canvas.width

    override def translate(x: Int, y: Int): Unit = {
      context.translate(x, y)
    }

    //probably useful to add to the core abstraction at some point
    //can save current translation/rotation, usefull for stack style
    //rendering
    def save(): Unit = {
      context.save()
    }
    def restore(): Unit = {
      context.restore()
    }

    override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      context.restore()
      context.save()
      context.beginPath()
      context.rect(x, y, width, height)
      context.clip()
    }

    override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = {
      context.drawImage(bitmap.image, x, y)
    }

    override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit = {
      context.drawImage(bitmap.image, sx, sy, width, height, dx, dy, width, height)
    }

    override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      paint.prepareContext(context)
      context.fillRect(x, y, width, height)
    }

    private def drawEllipse(x: Int, y: Int, w: Int, h: Int): Unit = {
      val kappa = 0.5522848
      val ox = (w / 2) * kappa // control point offset horizontal
      val oy = (h / 2) * kappa // control point offset vertical
      val xe = x + w           // x-end
      val ye = y + h           // y-end
      val xm = x + w / 2       // x-middle
      val ym = y + h / 2       // y-middle

      context.beginPath();
      context.moveTo(x, ym);
      context.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);
      context.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);
      context.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);
      context.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);
      //ctx.closePath(); // not used correctly, see comments (use to close off open path)
      context.fill();
    }

    override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      paint.prepareContext(context)
      drawEllipse(x, y, width, height)
    }

    override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = {
      paint.prepareContext(context)
      context.beginPath()
      context.moveTo(x1, y1)
      context.lineTo(x2, y2)
      context.stroke()
    }

    override def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = {
      paint.prepareContext(context)
      context.fillText(str, x, y)
    }

    override def drawText(text: TextLayout, x: Int, y: Int): Unit = ???

    override def drawColor(color: Color): Unit = {
      context.fillStyle = color
      context.fillRect(0, 0, WindowWidth, WindowHeight)
    }

    override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      context.clearRect(x, y, width, height)
    }

    override def renderText(text: String, width: Int, paint: Paint): TextLayout = ???
  }
  type Canvas = Html5Canvas


  var canvas: html.Canvas = null

  def getScreenCanvas: Canvas = Html5Canvas(canvas)
  def releaseScreenCanvas(canvas: Canvas): Unit = {
    //canvas.context.stroke()
  }

  type TextLayout = Html5TextLayout
  case class Html5TextLayout() extends AbstractTextLayout {
    override def height: Int = ???
  }
}
