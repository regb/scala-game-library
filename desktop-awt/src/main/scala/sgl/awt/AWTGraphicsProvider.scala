package sgl
package awt

import java.awt.{Image, Graphics, Graphics2D, Color}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.FontMetrics

trait AWTGraphicsProvider extends GraphicsProvider with Lifecycle {
  this: AWTWindowProvider =>

  abstract override def startup(): Unit = {
    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
  }


  case class AWTBitmap(img: Image) extends AbstractBitmap {
    override def height: Int = img.getHeight(null)
    override def width: Int = img.getWidth(null)
  }
  type Bitmap = AWTBitmap

  override def loadImageFromResource(path: String): Bitmap = {
    val url = getClass.getClassLoader.getResource(path)
    val bi = ImageIO.read(url)
    AWTBitmap(bi)
  }

  case class AWTFont(f: java.awt.Font) extends AbstractFont {
    override def withSize(s: Int): Font = AWTFont(f.deriveFont(f.getStyle, s))
    override def withStyle(s: Font.Style): Font = AWTFont(f.deriveFont(AWTFontCompanion.toAWTStyle(s)))

    override def isBold: Boolean = (f.getStyle & java.awt.Font.BOLD) != 0
    override def isItalic: Boolean = (f.getStyle & java.awt.Font.ITALIC) != 0
  }
  type Font = AWTFont

  object AWTFontCompanion extends FontCompanion {
    import java.awt.Font._

    override def create(family: String, style: Style, size: Int): Font =
      AWTFont(new java.awt.Font(family, toAWTStyle(style), size))

    def toAWTStyle(style: Style): Int = style match {
      case Bold => BOLD
      case Italic => ITALIC
      case Normal => PLAIN
      case BoldItalic => BOLD | ITALIC
    }
    //private def convertAWTStyle(awtStyle: Int): Style = awtStyle match {
    //  case BOLD => Bold
    //  case ITALIC => Italic
    //  case PLAIN => Normal
    //  case x if x == BOLD | ITALIC => BoldItalic
    //  case _ => Normal
    //}

    override val Default: Font = AWTFont(new java.awt.Font(DIALOG, PLAIN, 14))
    override val DefaultBold: Font = AWTFont(new java.awt.Font(DIALOG, BOLD, 14))
    override val Monospace: Font = AWTFont(new java.awt.Font(MONOSPACED, PLAIN, 14))
    override val SansSerif: Font = AWTFont(new java.awt.Font(SANS_SERIF, PLAIN, 14))
    override val Serif: Font = AWTFont(new java.awt.Font(SERIF, PLAIN, 14))

  }
  override val Font = AWTFontCompanion

  type Color = java.awt.Color
  object AWTColorCompanion extends ColorCompanion {
    override def rgb(r: Int, g: Int, b: Int): Color = new java.awt.Color(r, g, b)
    override def rgba(r: Int, g: Int, b: Int, a: Int): Color = new java.awt.Color(r, g, b, a)
  }
  override val Color = AWTColorCompanion

  case class AWTPaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
    def withFont(f: Font) = copy(font = f)
    def withColor(c: Color) = copy(color = c)
    def withAlignment(a: Alignments.Alignment) = copy(alignment = a)
  }

  type Paint = AWTPaint
  override def defaultPaint: Paint = AWTPaint(Font.Default, Color.Black, Alignments.Left)

  case class AWTCanvas(graphics: Graphics2D, var width: Int, var height: Int) extends AbstractCanvas {

    override def translate(x: Int, y: Int): Unit = {
      //using .toDouble is important as there is a Graphics2D method on Int, but it does not
      //modify the current translation and instead set absolute x and y positions
      graphics.translate(x.toDouble, y.toDouble)
    }

    override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      graphics.setClip(x, y, width, height)
      this.width = width
      this.height = height
    }

    override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = {
      graphics.drawImage(bitmap.img, x, y, null)
    }
    override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit = {
      graphics.drawImage(bitmap.img, dx, dy, dx+width, dy+height, sx, sy, sx+width, sy+height, null)
    }

    override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      graphics.setColor(paint.color)
      graphics.fillRect(x, y, width, height)
    }

    //override def drawRoundRect(x: Int, y: Int, width: Int, height: Int, rx: Float, ry: Float, paint: Paint): Unit = {
    //  graphics.setColor(paint.color)
    //  graphics.fillRoundRect(x, y, width, height, (rx*2).toInt, (ry*2).toInt)
    //}

    override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      graphics.setColor(paint.color)
      graphics.fillOval(x-width/2, y-height/2, width, height)
    }
    override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = {
      graphics.setColor(paint.color)
      graphics.drawLine(x1, y1, x2, y2)
    }

    override def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = {
      graphics.setColor(paint.color)
      graphics.setFont(paint.font.f)
      paint.alignment match {
        case Alignments.Center =>
          drawCenteredString(str, x, y, paint)
        case Alignments.Right =>
          drawRightAlignedString(str, x, y, paint)
        case Alignments.Left =>
          graphics.drawString(str, x, y)
      }
    }

    private def drawCenteredString(str: String, x: Int, y: Int, paint: Paint) {
      val metrics = graphics.getFontMetrics
      val realX = x - metrics.stringWidth(str)/2
      //val y = ((rect.height - metrics.getHeight()) / 2) - metrics.getAscent();
      graphics.drawString(str, realX, y)
    }
    private def drawRightAlignedString(str: String, x: Int, y: Int, paint: Paint) {
      val metrics = graphics.getFontMetrics
      val realX = x - metrics.stringWidth(str)
      graphics.drawString(str, realX, y)
    }

    override def drawText(text: TextLayout, x: Int, y: Int): Unit = {
      graphics.setColor(text.paint.color)
      graphics.setFont(text.paint.font.f)
      text.draw(graphics, x, y)
    }

    override def drawColor(color: Color): Unit = {
      graphics.setColor(color)
      graphics.fillRect(0, 0, width, height)
    }


    override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      graphics.clearRect(x, y, width, height)
    }

    override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
      graphics.setColor(paint.color)
      graphics.setFont(paint.font.f)
      AWTTextLayout(text, width, graphics.getFontMetrics, paint)
    }
  }
  type Canvas = AWTCanvas


  private var image1: BufferedImage = null
  private var image2: BufferedImage = null
  private var flip: Boolean = true

  def getScreenCanvas: Canvas = {
    //lazy initialization since width/height are not known during startup
    if(image1 == null) {
      image1 = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB)
      image2 = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB)
    }

    flip = !flip
    val g = if(flip) image1.getGraphics else image2.getGraphics
    AWTCanvas(g.asInstanceOf[Graphics2D], gamePanel.getWidth, gamePanel.getHeight)
  }
  def releaseScreenCanvas(canvas: Canvas): Unit = {
    val imageToDraw = if(flip) image1 else image2
    val g = gamePanel.getGraphics
    g.drawImage(imageToDraw, 0, 0, null)
    g.dispose()
  }


  type TextLayout = AWTTextLayout
  case class AWTTextLayout(text: String, width: Int, textMetrics: FontMetrics, paint: Paint) extends AbstractTextLayout {

    private val lineHeight = textMetrics.getHeight

    /* 
     * Split the text into rows, each row taking as 
     * much space as available, ready to be drawn
     */
    val rows: List[String] = {
      var res = new scala.collection.mutable.ListBuffer[String]()

      val lines = text.split("\n")
      for(line <- lines) {
        val words = line.split(" ")

        var nIndex = 0

        while(nIndex < words.length) {
          var currentLine = words(nIndex)
          nIndex += 1
          while(nIndex < words.length && textMetrics.stringWidth(currentLine + " " + words(nIndex)) < width) {
            currentLine = currentLine + " " + words(nIndex)
            nIndex += 1
          }
          res.append(currentLine)
        }
      }

      res.toList
    }

    override val height: Int = rows.size * lineHeight

    def draw(g: Graphics2D, x: Int, y: Int): Unit = {
      var startY = y
      rows.foreach(line => {
        g.drawString(line, x, startY)
        startY += lineHeight
      })
    }

    //def renderText(text: String, x: Int, y: Int, width: Int, textMetrics: FontMetrics, g: Graphics): Unit = {

    //  var startX = x
    //  var startY = y

    //  val lines = text.split("\n")
    //  for(line <- lines) {
    //    val words = line.split(" ")

    //    var nIndex = 0

    //    while(nIndex < words.length) {
    //      var line = words(nIndex)
    //      nIndex += 1
    //      while(nIndex < words.length && textMetrics.stringWidth(line + " " + words(nIndex)) < width) {
    //        line = line + " " + words(nIndex)
    //        nIndex += 1
    //      }
    //      g.drawString(line, startX, startY);
    //      startY = startY + lineHeight
    //    }
    //  }
    //}

  }
}
