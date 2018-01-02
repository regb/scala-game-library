package sgl
package html5

import sgl.util._

import org.scalajs.dom
import dom.html
import dom.raw.HTMLImageElement

trait Html5GraphicsProvider extends GraphicsProvider {
  this: Html5WindowProvider with Html5SystemProvider =>

  object Html5Graphics extends Graphics {

    override def loadImage(path: ResourcePath): Loader[Bitmap] = {
      println("image loading!")
      val p = new DefaultLoader[Bitmap]()
      val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      img.onload = (e: dom.Event) => {
        p.success(Html5Bitmap(img))
      }
      img.addEventListener("error", (e: dom.Event) => {
        p.failure(new RuntimeException("image failed to load"))
      })
      img.src = s"static${path.path}"
      p.loader
    }

    //could wrap that into a LoaderProxy, that makes sure image is loaded before drawing it
    case class Html5Bitmap(image: HTMLImageElement) extends AbstractBitmap {
      override def height: Int = image.height
      override def width: Int = image.width
    }
    type Bitmap = Html5Bitmap

    //TODO: seems like I could define Font.Style as an abstract type (not an enum) and
    //      have it concretely implemented here as a string directly, that would optimize
    //      a bit in the optimized js, as all indirect abstraction should be removed
    //      and we will directly manipulate the string representing the font style
    //
    //      Btw, this likely applies to Alignment and a bunch of other stuff as well
    case class Html5Font(family: String, style: Font.Style, size: Int) extends AbstractFont {
      override def withSize(s: Int): Font = copy(size = s)
      override def withStyle(s: Font.Style): Font = copy(style = s)

      override def isBold: Boolean = style == Font.Bold || style == Font.BoldItalic
      override def isItalic: Boolean = style == Font.Italic || style == Font.BoldItalic

      def asCss: String = {
        //not sure, but seems that the ordering of size/style/family is important.
        //"normal 20px sans-serif" seems to be the most reasonable
        val scss = Font.toCssStyle(style)
        s"$scss ${size}px $family"
      }
    }
    type Font = Html5Font

    object Html5FontCompanion extends FontCompanion {

      override def create(family: String, style: Style, size: Int): Font = Html5Font(family, style, size)


      def toCssStyle(s: Font.Style): String = s match {
        case Bold => "bold"
        case Italic => "italic"
        case Normal => "normal"
        case BoldItalic => "italic bold"
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
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = {
        val alpha = a/255d
        s"rgba($r,$g,$b,$alpha)"
      }
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

      override def height: Int = canvas.height
      override def width: Int = canvas.width

      //note that the scala.js compiler is able to inline the body, so
      //you don't pay any performance cost for using the nice auto wrapping
      //syntax
      override def withSave[A](body: => A): A = {
        context.save()
        val res = body
        context.restore()
        res
      }

      override def translate(x: Int, y: Int): Unit = {
        context.translate(x, y)
      }

      override def rotate(theta: Double): Unit = {
        //rotate towards positive x/y (so, visually clockwise)
        context.rotate(theta)
      }

      override def scale(sx: Double, sy: Double): Unit = {
        context.scale(sx, sy)
      }


      override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
        context.beginPath()
        context.rect(x, y, width, height)
        context.clip()
      }

      override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = {
        context.drawImage(bitmap.image, x, y)
      }

      override def drawBitmap(bitmap: Bitmap, x: Int, y: Int, s: Float): Unit = {
        drawBitmap(bitmap, x, y, 0, 0, width, height, s)
      }

      override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f): Unit = {
        context.drawImage(bitmap.image, sx, sy, width, height, dx, dy, s*width, s*height)
      }

      override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
        paint.prepareContext(context)
        context.fillRect(x, y, width, height)
      }

      //drawing an ellipsis, with x,y top-left
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
        drawEllipse(x-width/2, y-height/2, width, height)
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

      override def drawText(text: TextLayout, x: Int, y: Int): Unit = {
        text.draw(context, x, y)
      }

      override def drawColor(color: Color): Unit = {
        context.fillStyle = color
        context.fillRect(0, 0, WindowWidth, WindowHeight)
      }

      override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
        context.clearRect(x, y, width, height)
      }

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        Html5TextLayout(text, width, context, paint)
      }
    }
    type Canvas = Html5Canvas

    type TextLayout = Html5TextLayout
    case class Html5TextLayout(text: String, width: Int, context: Ctx2D, paint: Paint) extends AbstractTextLayout {

      paint.prepareContext(context)

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
            while(nIndex < words.length && context.measureText(currentLine + " " + words(nIndex)).width < width) {
              currentLine = currentLine + " " + words(nIndex)
              nIndex += 1
            }
            res.append(currentLine)
          }
        }

        res.toList
      }

      //TODO: maybe need to add a few extra px? Need to check exact specs of what is
      //      the line height with respect to the font size
      //      Also need to make sure we are consistent across all platforms
      private val lineHeight = paint.font.size

      override val height: Int = rows.size * lineHeight

      def draw(ctx: Ctx2D, x: Int, y: Int): Unit = {
        paint.prepareContext(ctx)
        var startY = y
        rows.foreach(line => {
          ctx.fillText(line, x, startY)
          startY += lineHeight
        })
      }

    }
  }

  override val Graphics = Html5Graphics

}
