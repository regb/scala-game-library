package sgl
package awt

import sgl.util._

import java.awt.{RenderingHints, FontMetrics, Graphics2D, AlphaComposite, GraphicsEnvironment, Transparency}
import java.awt.image.BufferedImage
import java.awt.geom.{Rectangle2D, Ellipse2D, Line2D, AffineTransform}
import javax.imageio.ImageIO

import scala.collection.mutable

trait AWTCanvasProvider extends CanvasProvider {
  this: AWTWindowProvider with DesktopSystemProvider with LoggingProvider =>

  lazy val AWTGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
  lazy val AWTGraphicsConfig = AWTGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration

  object AWTGraphics extends Graphics {

    override def loadImage(asset: sgl.assets.DrawableAsset): Loader[Bitmap] = {
      val variant = asset.bestVariantForDpi(Window.logicalPpi)
      val sourceDpi = ScreenDensity.fromName(variant.density).map(_.dpi.toFloat).getOrElse(160f)
      loadImageResource(variant.resourceName, sourceDpi)
    }

    private def loadImageResource(resourceName: String, sourceDpi: Float): Loader[Bitmap] = FutureLoader {
      val url = getClass.getClassLoader.getResource(resourceName)
      if(url == null) {
        throw new ResourceNotFoundException(PartsResourcePath(resourceName.split('/').toVector))
      }

      val scalingFactor = Window.logicalPpi / sourceDpi

      val bufferedImage = {
        val sb = ImageIO.read(url)

        val scaledWidth = (scalingFactor*sb.getWidth).toInt
        val scaledHeight = (scalingFactor*sb.getHeight).toInt
        // Now we copy the read buffered image into a new buffered image which
        // has a compatibly TYPE_INT_ARGB with the target buffer in which we will
        // render the image.
        val b = AWTGraphicsConfig.createCompatibleImage(scaledWidth, scaledHeight, Transparency.TRANSLUCENT)
        val g = b.createGraphics
        g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g.drawImage(sb, 0, 0, scaledWidth, scaledHeight, null)
        g.dispose()
        sb.flush()
        b
      }

      AWTBitmap(bufferedImage)
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

    case class AWTFont(f: java.awt.Font, portable: Boolean = false) extends AbstractFont {
      override def withSize(s: Int): Font = copy(f = f.deriveFont(f.getStyle, s.toFloat))
      override def withStyle(s: Font.Style): Font = copy(f = f.deriveFont(AWTFontCompanion.toAWTStyle(s)))

      override def size: Int = f.getSize

      override def isBold: Boolean = (f.getStyle & java.awt.Font.BOLD) != 0
      override def isItalic: Boolean = (f.getStyle & java.awt.Font.ITALIC) != 0
    }
    type Font = AWTFont

    object AWTFontCompanion extends FontCompanion {
      import java.awt.Font._

      override def load(asset: sgl.assets.FontAsset): Loader[Font] = {
        FutureLoader {
          val res = getClass.getClassLoader.getResourceAsStream(asset.resourceName)
          if(res == null) {
            throw new ResourceNotFoundException(PartsResourcePath(asset.resourceName.split('/').toVector))
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

      override val Default: Font = AWTFont(new java.awt.Font(SANS_SERIF, PLAIN, 14), portable = true)
      override val DefaultBold: Font = AWTFont(new java.awt.Font(SANS_SERIF, BOLD, 14), portable = true)
      override val Monospace: Font = AWTFont(new java.awt.Font(MONOSPACED, PLAIN, 14), portable = true)
      override val SansSerif: Font = AWTFont(new java.awt.Font(SANS_SERIF, PLAIN, 14), portable = true)
      override val Serif: Font = AWTFont(new java.awt.Font(SERIF, PLAIN, 14), portable = true)

    }
    override val Font: AWTFontCompanion.type = AWTFontCompanion

    type Color = java.awt.Color
    object AWTColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = new java.awt.Color(r, g, b)
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = new java.awt.Color(r, g, b, a)
    }
    override val Color: AWTColorCompanion.type = AWTColorCompanion

    case class AWTPaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
      def withFont(f: Font) = copy(font = f)
      def withColor(c: Color) = copy(color = c)
      def withAlignment(a: Alignments.Alignment) = copy(alignment = a)
    }

    type Paint = AWTPaint
    override def defaultPaint: Paint = AWTPaint(Font.Default, Color.Black, Alignments.Left)

    private val portableAtlasCache = mutable.Map.empty[(Boolean, Int), BufferedImage]

    private def portableMeasure(value: String, font: Font): Float =
      PortableFont.measure(value, PortableFont.face(font.isBold), font.size)

    private def portableAtlas(font: Font, color: Color): BufferedImage = {
      val key = (font.isBold, color.getRGB)
      portableAtlasCache.getOrElseUpdate(key, {
        val face = PortableFont.face(font.isBold)
        val image = new BufferedImage(face.atlasWidth, face.atlasHeight, BufferedImage.TYPE_INT_ARGB)
        val pixels = new Array[Int](face.alpha.length)
        val colorAlpha = color.getAlpha
        var index = 0
        while(index < pixels.length) {
          val alpha = (face.alpha(index) & 0xff) * colorAlpha / 255
          pixels(index) = (alpha << 24) | (color.getRed << 16) | (color.getGreen << 8) | color.getBlue
          index += 1
        }
        image.setRGB(0, 0, face.atlasWidth, face.atlasHeight, pixels, 0, face.atlasWidth)
        image
      })
    }

    private def drawPortableString(graphics: Graphics2D, value: String, x: Float, baselineY: Float, paint: Paint): Unit = {
      val face = PortableFont.face(paint.font.isBold)
      val fontScale = PortableFont.scale(paint.font.size)
      val startX = paint.alignment match {
        case Alignments.Left => x
        case Alignments.Center => x - portableMeasure(value, paint.font) / 2f
        case Alignments.Right => x - portableMeasure(value, paint.font)
      }
      val atlas = portableAtlas(paint.font, paint.color)
      val textGraphics = graphics.create().asInstanceOf[Graphics2D]
      textGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      try {
        var cursorX = startX
        PortableFont.foreachCodePoint(value) { codePoint =>
          val glyph = face.glyph(codePoint)
          if(glyph.width > 0 && glyph.height > 0) {
            val glyphImage = atlas.getSubimage(glyph.x, glyph.y, glyph.width, glyph.height)
            val transform = new AffineTransform
            transform.translate(cursorX + glyph.bearingX * fontScale, baselineY + glyph.bearingY * fontScale)
            transform.scale(fontScale, fontScale)
            textGraphics.drawImage(glyphImage, transform, null)
          }
          cursorX += glyph.advance * fontScale
        }
      } finally textGraphics.dispose()
    }

    case class AWTCanvas(var graphics: Graphics2D, var width: Float, var height: Float) extends AbstractCanvas {
      // We keep the virtual width/height so that we can set the proper rectangles when we need
      // to draw the whole visible area. Note that this means that the drawColor only draws the
      // current canvas and not the theoretically infinite space.

      // A temporary rectangle used for calling the Graphics2D APIs. We
      // try to help the garbage collector by only instantiating once and
      // reusing it in all calls.
      private val rect = new Rectangle2D.Float(0, 0, 0, 0)
      private val ellipse = new Ellipse2D.Float(0, 0, 0, 0)
      private val line = new Line2D.Float(0, 0, 0, 0)
      private val affineTransform = new AffineTransform

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

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit = {
        val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        graphics.setComposite(ac)
        
        // Save the current clip, before setting the clip for the draw area.
        val c = graphics.getClip
        rect.setRect(dx, dy, dw, dh)
        graphics.clip(rect)

        affineTransform.setToIdentity()
        affineTransform.translate(dx, dy)
        affineTransform.scale(dw/sw, dh/sh)
        affineTransform.translate(-sx, -sy)
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
        // Unforutnately, I have also observed that drawing the outline can make the rect appear
        // bigger when using some scaling (drawing rect under different scaling will not be aligned
        // even if theoretically they should be on the same y location).
        // TODO: I need to find the correct specs and define the drawRect accordingly.
        //graphics.draw(rect)

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
        if(paint.font.portable) drawPortableString(graphics, str, x, y, paint)
        else {
          graphics.setColor(paint.color)
          graphics.setFont(paint.font.f)
          val metrics = graphics.getFontMetrics
          val realX = paint.alignment match {
            case Alignments.Left => x
            case Alignments.Center => x - metrics.stringWidth(str) / 2f
            case Alignments.Right => x - metrics.stringWidth(str)
          }
          graphics.drawString(str, realX, y)
        }
      }

      override def drawText(text: TextLayout, x: Float, y: Float): Unit = {
        graphics.setColor(text.paint.color)
        graphics.setFont(text.paint.font.f)
        text.draw(graphics, x, y)
      }

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        graphics.setColor(paint.color)
        graphics.setFont(paint.font.f)
        AWTTextLayout(text, width, graphics.getFontMetrics, paint)
      }
    }
    type Canvas = AWTCanvas

    type TextLayout = AWTTextLayout
    case class AWTTextLayout(text: String, layoutWidth: Int, textMetrics: FontMetrics, paint: Paint) extends AbstractTextLayout {

      private val portableFace = PortableFont.face(paint.font.isBold)
      private val portableScale = PortableFont.scale(paint.font.size)
      private def measuredWidth(value: String): Float =
        if(paint.font.portable) portableMeasure(value, paint.font) else textMetrics.stringWidth(value).toFloat
      private val wrapped = TextWrapping.wrap(text, layoutWidth, measuredWidth)

      override val lines: Vector[String] = wrapped.lines
      val rows: Seq[String] = lines
      override val overflowed: Boolean = wrapped.overflowed
      override val lineCount: Int = lines.size
      override val lineHeight: Int =
        if(paint.font.portable) scala.math.ceil(portableFace.lineHeight * portableScale).toInt else textMetrics.getHeight
      override val ascent: Int =
        if(paint.font.portable) scala.math.ceil(portableFace.ascent * portableScale).toInt else textMetrics.getAscent
      override val descent: Int =
        if(paint.font.portable) scala.math.ceil(portableFace.descent * portableScale).toInt else textMetrics.getDescent
      override val width: Int = scala.math.ceil(lines.foldLeft(0f)((maximum, line) => scala.math.max(maximum, measuredWidth(line)))).toInt
      override val height: Int = ascent + descent + (lineCount - 1) * lineHeight

      def draw(g: Graphics2D, x: Float, y: Float): Unit = {
        var baseline = y + ascent
        lines.foreach { line =>
          val lineWidth = measuredWidth(line)
          val lineX = paint.alignment match {
            case Alignments.Left => x
            case Alignments.Center => x + (layoutWidth - lineWidth) / 2f
            case Alignments.Right => x + layoutWidth - lineWidth
          }
          if(paint.font.portable)
            drawPortableString(g, line, lineX, baseline, paint.copy(alignment = Alignments.Left))
          else
            g.drawString(line, lineX, baseline)
          baseline += lineHeight
        }
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
  override val Graphics: AWTGraphics.type = AWTGraphics

}
