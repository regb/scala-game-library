package sgl
package awt

import sgl.util._
import awt.util._

import java.awt.{FontMetrics, Image, Graphics, Graphics2D, Color, AlphaComposite, GraphicsEnvironment, GraphicsConfiguration, Transparency}
import java.awt.image.BufferedImage
import java.awt.geom.{Rectangle2D, Ellipse2D, Line2D, AffineTransform}
import javax.imageio.ImageIO

trait AWTGraphicsProvider extends GraphicsProvider {
  this: AWTWindowProvider with AWTSystemProvider =>

  val AWTGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
  val AWTGraphicsConfig = AWTGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration

  object AWTGraphics extends Graphics {

    override def loadImage(path: ResourcePath): Loader[Bitmap] = {
      FutureLoader {
        val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
        val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
        if(url == null) {
          throw new ResourceNotFoundException(path)
        }

        val bufferedImage = {
          val sb = ImageIO.read(url)
          // Now we copy the read buffered image into a new buffered image which
          // has a compatibly TYPE_INT_ARGB with the target buffer in which we will
          // render the image.
          val b = AWTGraphicsConfig.createCompatibleImage(sb.getWidth, sb.getHeight, Transparency.TRANSLUCENT)
          val g = b.createGraphics
          g.drawImage(sb, 0, 0, null)
          g.dispose()
          sb.flush()
          b
        }

        AWTBitmap(bufferedImage)
      }
    }

    case class AWTBitmap(var img: BufferedImage) extends AbstractBitmap {
      override def height: Int = img.getHeight(null)
      override def width: Int = img.getWidth(null)

      override def release(): Unit = {
        img.flush()
        // Make sure we lose the pointer for garbage collection, in case the game
        // kept a pointer to the AWTBitmap.
        img = null
      }

    }
    type Bitmap = AWTBitmap

    case class AWTFont(f: java.awt.Font) extends AbstractFont {
      override def withSize(s: Int): Font = AWTFont(f.deriveFont(f.getStyle, s))
      override def withStyle(s: Font.Style): Font = AWTFont(f.deriveFont(AWTFontCompanion.toAWTStyle(s)))

      override def size: Int = f.getSize

      override def isBold: Boolean = (f.getStyle & java.awt.Font.BOLD) != 0
      override def isItalic: Boolean = (f.getStyle & java.awt.Font.ITALIC) != 0
    }
    type Font = AWTFont

    object AWTFontCompanion extends FontCompanion {
      import java.awt.Font._

      override def load(path: ResourcePath): Loader[Font] = {
        FutureLoader {
          val res = getClass.getClassLoader.getResourceAsStream(path.path)
          if(res == null) {
            throw new ResourceNotFoundException(path)
          }
          val f = createFont(TRUETYPE_FONT, res)
          AWTFont(f)
        }
      }

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

    case class AWTCanvas(var graphics: Graphics2D, var width: Float, var height: Float) extends AbstractCanvas {
      // We keep the virtual width/height so that we can set the proper rectangles when we need
      // to draw the whole visible area. Note that this means that the drawColor only draws the
      // current canvas and not the theoretically infinite space.

      // A temporary rectangle used for calling the Graphics2D APIs. We
      // try to help the garbage collector by only instantiating once and
      // reusing it in all calls.
      private var rect = new Rectangle2D.Float(0, 0, 0, 0)
      private var ellipse = new Ellipse2D.Float(0, 0, 0, 0)
      private var line = new Line2D.Float(0, 0, 0, 0)
      private var affineTransform = new AffineTransform

      override def withSave[A](body: => A): A = {
        // Save current state.
        val oldGraphics: Graphics2D = graphics.create().asInstanceOf[Graphics2D]
        val oldWidth = this.width
        val oldHeight = this.height

        // Execute the body and bind the res.
        val res = body

        // Restore saved state.
        this.graphics = oldGraphics
        this.width = oldWidth
        this.height = oldHeight

        res
      }

      override def translate(x: Float, y: Float): Unit = {
        //using .toDouble is important as there is a Graphics2D method on Int, but it does not
        //modify the current translation and instead set absolute x and y positions
        graphics.translate(x, y)
      }

      override def rotate(theta: Float): Unit = {
        // graphics.rotate states that rotating with a positive angle theta (in
        // radians) rotates points on the positive x axis toward the positive y
        // axis.
        graphics.rotate(theta)
      }

      override def scale(sx: Float, sy: Float): Unit = {
        graphics.scale(sx, sy)
        // Scaling means that drawing will be scaled up, it has the inverse
        // effect on the visible width/height of the canvas (they are scaled
        // down by the same factors).
        this.width = width/sx
        this.height = height/sy
      }

      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = {
        rect.setRect(x, y, width, height)
        graphics.clip(rect)
        // According to doc of clipRect, we don't want to change the width/height on clipping.
        // Not sure all the backends are respecting that though?
      }

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f, alpha: Float = 1f): Unit = {
        val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        graphics.setComposite(ac)
        
        // Save the current clip, before setting the clip for the draw area.
        val c = graphics.getClip
        rect.setRect(dx, dy, width*s, height*s)
        graphics.clip(rect)

        affineTransform.setToIdentity()
        affineTransform.translate(dx - sx, dy - sy)
        affineTransform.scale(s, s)
        graphics.drawImage(bitmap.img, affineTransform, null)
        
        graphics.setClip(c)

        // Reset default alpha composite.
        val dac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
        graphics.setComposite(dac)
      }

      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        graphics.setColor(paint.color)
        rect.setRect(x, y, width, height)
        // I have noticed that under some scaling, if we only fill the rectangle, this can lead to
        // a tiny space between the rect and the tiles. Using both draw and fill seems to fix it.
        // I wasn't able to find the proper documentation on that, but it did fix a problem and
        // it does not seem to create any, so I'm going with the double draw.
        graphics.draw(rect)
        graphics.fill(rect)
      }

      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        graphics.setColor(paint.color)
        ellipse.x = x-width/2
        ellipse.y = y-height/2
        ellipse.width = width
        ellipse.height = height
        graphics.fill(ellipse)
      }

      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = {
        graphics.setColor(paint.color)
        line.setLine(x1, y1, x2, y2)
        graphics.draw(line)
      }

      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit = {
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

      private def drawCenteredString(str: String, x: Float, y: Float, paint: Paint) {
        val metrics = graphics.getFontMetrics
        val realX = x - metrics.stringWidth(str)/2
        //val y = ((rect.height - metrics.getHeight()) / 2) - metrics.getAscent();
        graphics.drawString(str, realX, y)
      }
      private def drawRightAlignedString(str: String, x: Float, y: Float, paint: Paint) {
        val metrics = graphics.getFontMetrics
        val realX = x - metrics.stringWidth(str)
        graphics.drawString(str, realX, y)
      }

      override def drawText(text: TextLayout, x: Float, y: Float): Unit = {
        graphics.setColor(text.paint.color)
        graphics.setFont(text.paint.font.f)
        text.draw(graphics, x, y)
      }

      override def drawColor(color: Color): Unit = {
        graphics.setColor(color)
        rect.setRect(0, 0, width, height)
        graphics.fill(rect)
      }


      //override def clearRect(x: Float, y: Float, width: Float, height: Float): Unit = {
      //  graphics.setColor(Color.Black)
      //  rect.setRect(x, y, width, height)
      //  graphics.fill(rect)
      //}

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        graphics.setColor(paint.color)
        graphics.setFont(paint.font.f)
        AWTTextLayout(text, width, graphics.getFontMetrics, paint)
      }
    }
    type Canvas = AWTCanvas

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

      def draw(g: Graphics2D, x: Float, y: Float): Unit = {
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
  override val Graphics = AWTGraphics

}
