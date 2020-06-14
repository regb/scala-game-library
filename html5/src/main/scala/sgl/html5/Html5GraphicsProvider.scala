package sgl
package html5

import sgl.util._

import scala.scalajs.js
import org.scalajs.dom
import dom.html
import dom.raw.HTMLImageElement

trait Html5GraphicsProvider extends GraphicsProvider {
  this: Html5WindowProvider with Html5SystemProvider =>

  object Html5Graphics extends Graphics {

    private def imageExists(path: ResourcePath): Boolean = {
      var http = new dom.XMLHttpRequest()
      http.open("HEAD", path.path, false)
      http.send()
      http.status != 404
    }

    private def dpiToRatio(dpi: String): Double = dpi match {
      case "mdpi" => 1d
      case "hdpi" => 1.5d
      case "xhdpi" => 2d
    }

    private def bestDPIs(pixelRatio: Double): Seq[String] = {
      if(pixelRatio == 1d) Seq("mdpi", "hdpi", "xhdpi")
      else if(pixelRatio == 1.5d) Seq("hdpi", "mdpi", "xhdpi")
      else if(pixelRatio == 2d) Seq("xhdpi", "hdpi", "mdpi")
      else if(pixelRatio == 0.5d) Seq("mdpi", "hdpi", "xhdpi")
      else {
        if(pixelRatio < 1.5d) Seq("mdpi", "hdpi", "xhdpi")
        else if(pixelRatio < 2d) Seq("hdpi", "xhdpi", "mdpi")
        else Seq("xhdpi", "hdpi", "mdpi")
      }
    }

    // Load an image resource which comes from resources for a given dpi.
    // If the path is for drawable-mdpi, the dpi will be mdpi, it's the role
    // of this function to make sure the bitmap is scaled if it needs to.
    private def tryLoadImageDpi(path: ResourcePath, dpi: String): Loader[Bitmap] = {
      val p = new DefaultLoader[Bitmap]()
      val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      img.addEventListener("load", (e: dom.Event) => {
        p.success(Html5Bitmap(img, dom.window.devicePixelRatio/dpiToRatio(dpi)))
      })
      img.addEventListener("error", (e: dom.Event) => {
        p.failure(new RuntimeException(s"image <${path.path}> failed to load"))
      })
      img.src = path.path
      p.loader
    }

    override def loadImage(path: ResourcePath): Loader[Bitmap] = {
      val options = bestDPIs(dom.window.devicePixelRatio)
      val pathes = options.map(dpi => (dpi, PartsResourcePath(Vector("static", s"drawable-$dpi") ++ path.parts)))

      // We try to load the image with each alternative, only starting to load a new one if the
      // previous one failed. This should be more efficient than using a HEAD request to check
      // if a resource is available, because if it isn't there, the loading should fail right
      // away (nothing to load anyway), and if it doesn't fail, then we get the best image
      // resource anyway, so it was worth trying to load.
      pathes.foldLeft(
        Loader.failed[Bitmap](new RuntimeException(s"Found no resources for image ${path.path}"))
      ){ case (r, (dpi, path)) => r fallbackTo tryLoadImageDpi(path, dpi) }
    }

    /* 
     * The HTMLImageElement might need to be scaled, if we are on a higher density but we
     * could not find the ideal resource, we will use canvas scaling once in order to get
     * a scaled image to be used in the rest of the system.
     */
    case class Html5Bitmap(image: HTMLImageElement, scaling: Double) extends AbstractBitmap {
      val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
      canvas.width = this.width
      canvas.height = this.height
      canvas.getContext("2d").drawImage(image, 0, 0, canvas.width, canvas.height)

      override def height: Int = (image.height*scaling).toInt
      override def width: Int = (image.width*scaling).toInt

      override def release(): Unit = {}
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

      private var fontId = -1
      private object fontLock

      override def load(path: ResourcePath): Loader[Font] = {
        val fontName = fontLock.synchronized {
          fontId += 1
          s"sgl-custom-font-$fontId"
        }

        val styleNode = dom.document.createElement("style")
        // TODO: find a generic way to set the correct format
        //       (opentype vs truetype vs other) A trivial but
        //       incomplete way would be to check the file extension.
        styleNode.textContent = raw"""
@font-face {
  font-family: '${fontName}';
  src: url('${path.path}') format("opentype");
}"""
        dom.document.body.appendChild(styleNode)

        // This is a hack to insert an invisible text node using the newly defined font.
        // This will force to load the font right away. Otherwise, the browser waits
        // on an actual usage of the font, so we cannot properly implement the loader
        // interface.
        val preloadNode = {
          val div = dom.document.createElement("div").asInstanceOf[html.Div]
          div.style.opacity = "0"
          val inner = dom.document.createElement("span").asInstanceOf[html.Span]
          inner.style.fontFamily = fontName
          // On Safari, the fonts will not autoload without actual text inside the <span> element
          // but on other browsers this seems optional.
          inner.textContent = "abcd"
          div.appendChild(inner)
          div
        }
        // We want to add this early in the body, in particular before the canvas, so that
        // it does not appear on top of the canvas and could be interecpting mouse events.
        dom.document.body.insertBefore(preloadNode, dom.document.body.firstChild)

        val loader = new DefaultLoader[Font]
        // TODO: we are not checking if the font failed to load (missing file or wrong format).
        //       Not clear what would happen if that was the case (maybe the loader would never
        //       complete?).
        def tryCompleteLoader(): Unit = {
          if(js.Dynamic.global.document.fonts.check(s"1em $fontName").asInstanceOf[Boolean]) {
            loader.success(Html5Font(fontName, Normal, 10))
          } else {
            dom.window.setTimeout(() => tryCompleteLoader, 30)
          }
        }
        dom.window.setTimeout(() => tryCompleteLoader, 30)
        loader
      }

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
      // The width/height are the transformed width/height of the canvas, if you
      // use the width/height properties of the canvas, that would return the
      // original canvas (or the real HTML physical dimensions on the original page).
      var width: Float = canvas.width
      var height: Float = canvas.height
      
      val context = canvas.getContext("2d").asInstanceOf[Ctx2D]

      //note that the scala.js compiler is able to inline the body, so
      //you don't pay any performance cost for using the nice auto wrapping
      //syntax
      override def withSave[A](body: => A): A = {
        val owidth = this.width
        val oheight = this.height
        context.save()

        val res = body

        context.restore()
        this.width = owidth
        this.height = oheight

        res
      }

      override def translate(x: Float, y: Float): Unit = {
        context.translate(x, y)
      }

      override def rotate(theta: Float): Unit = {
        //rotate towards positive x/y (so, visually clockwise)
        context.rotate(theta)
      }

      override def scale(sx: Float, sy: Float): Unit = {
        context.scale(sx, sy)
        this.width = this.width/sx
        this.height = this.height/sy
      }

      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = {
        context.beginPath()
        context.rect(x, y, width, height)
        context.clip()
      }

      override def drawBitmap(bitmap: Bitmap, x: Float, y: Float): Unit = {
        context.drawImage(bitmap.canvas, x, y)
      }

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit = {
        context.globalAlpha = alpha
        context.drawImage(bitmap.canvas, sx, sy, sw, sh, dx, dy, dw, dh)
        context.globalAlpha = 1f
      }

      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        paint.prepareContext(context)
        context.fillRect(x, y, width, height)
      }

      //drawing an ellipsis, with x,y top-left
      private def drawEllipse(x: Float, y: Float, w: Float, h: Float): Unit = {
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

      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        paint.prepareContext(context)
        drawEllipse(x-width/2, y-height/2, width, height)
      }

      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = {
        paint.prepareContext(context)
        context.beginPath()
        context.moveTo(x1, y1)
        context.lineTo(x2, y2)
        context.stroke()
      }

      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit = {
        paint.prepareContext(context)
        context.fillText(str, x, y)
      }

      override def drawText(text: TextLayout, x: Float, y: Float): Unit = {
        text.draw(context, x, y)
      }

      override def drawColor(color: Color): Unit = {
        context.save()
	context.setTransform(1, 0, 0, 1, 0, 0)
        context.fillStyle = color
        context.fillRect(0, 0, Window.width, Window.height)
	context.restore()
      }

      // override def clearRect(x: Float, y: Float, width: Float, height: Float): Unit = {
      //   context.clearRect(x, y, width, height)
      // }

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

      def draw(ctx: Ctx2D, x: Float, y: Float): Unit = {
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
