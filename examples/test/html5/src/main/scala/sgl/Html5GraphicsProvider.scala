package sgl
package html5

import org.scalajs.dom
import dom.html

trait Html5GraphicsProvider extends GraphicsProvider with Lifecycle {
  //this: AWTWindowProvider =>

  abstract override def startup(): Unit = {
    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
  }


  case class Html5Bitmap() extends AbstractBitmap {
    override def height: Int = ???
    override def width: Int = ???
  }
  type Bitmap = Html5Bitmap

  override def loadImageFromResource(path: String): Bitmap = ???


  case class Html5Font() extends AbstractFont {
    override def withSize(s: Int): Font = ???
    override def withStyle(s: Font.Style): Font = ???

    override def isBold: Boolean = ???
    override def isItalic: Boolean = ???
  }
  type Font = Html5Font

  object Html5FontCompanion extends FontCompanion {

    override def create(family: String, style: Style, size: Int): Font = ???

    //def toAWTStyle(style: Style): Int = style match {
    //  case Bold => BOLD
    //  case Italic => ITALIC
    //  case Normal => PLAIN
    //  case BoldItalic => BOLD | ITALIC
    //}
    //private def convertAWTStyle(awtStyle: Int): Style = awtStyle match {
    //  case BOLD => Bold
    //  case ITALIC => Italic
    //  case PLAIN => Normal
    //  case x if x == BOLD | ITALIC => BoldItalic
    //  case _ => Normal
    //}

    override val Default: Font = ???
    override val DefaultBold: Font = ???
    override val Monospace: Font = ???
    override val SansSerif: Font = ???
    override val Serif: Font = ???

  }
  override val Font = Html5FontCompanion

  type Color = Int
  object Html5ColorCompanion extends ColorCompanion {
    override def rgb(r: Int, g: Int, b: Int): Color = ???
    override def rgba(r: Int, g: Int, b: Int, a: Int): Color = ???
  }
  override val Color = Html5ColorCompanion

  case class Html5Paint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
    def withFont(f: Font) = copy(font = f)
    def withColor(c: Color) = copy(color = c)
    def withAlignment(a: Alignments.Alignment) = copy(alignment = a)
  }

  type Paint = Html5Paint
  override def defaultPaint: Paint = Html5Paint(Font.Default, Color.Black, Alignments.Left)

  type Ctx2D = dom.CanvasRenderingContext2D

  case class Html5Canvas(canvas: html.Canvas) extends AbstractCanvas {
    
    val context = canvas.getContext("2d").asInstanceOf[Ctx2D]

    override def height: Int = canvas.height
    override def width: Int = canvas.width

    override def translate(x: Int, y: Int): Unit = ???

    override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = ???

    override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = ???
    override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit = ???

    override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      context.strokeStyle = "black"
      context.fillRect(x, y, width, height)
    }

    override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = ???
    override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = ???

    override def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = ???

    override def drawText(text: TextLayout, x: Int, y: Int): Unit = ???

    override def drawColor(color: Color): Unit = ???

    override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = ???

    override def renderText(text: String, width: Int, paint: Paint): TextLayout = ???
  }
  type Canvas = Html5Canvas


  var canvas: html.Canvas = null

  def getScreenCanvas: Canvas = Html5Canvas(canvas)
  def releaseScreenCanvas(canvas: Canvas): Unit = {
    canvas.context.stroke()
  }

  type TextLayout = Html5TextLayout
  case class Html5TextLayout() extends AbstractTextLayout {
    override def height: Int = ???
  }
}
