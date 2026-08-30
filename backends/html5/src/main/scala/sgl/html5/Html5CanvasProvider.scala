package sgl
package html5

import sgl.util._

import scala.scalajs.js
import org.scalajs.dom
import dom.html
import dom.HTMLImageElement

trait Html5CanvasProvider extends CanvasProvider {
  this: Html5WindowProvider with Html5SystemProvider with Html5CanvasApp =>

  object Html5Graphics extends Graphics {

    private def dpiToRatio(dpi: String): Double = dpi match {
      case "mdpi" => 1d
      case "hdpi" => 1.5d
      case "xhdpi" => 2d
    }

    private def bestDPIs(pixelRatio: Double): List[String] = {
      if(pixelRatio == 1d) List("mdpi", "hdpi", "xhdpi")
      else if(pixelRatio == 1.5d) List("hdpi", "mdpi", "xhdpi")
      else if(pixelRatio == 2d) List("xhdpi", "hdpi", "mdpi")
      else if(pixelRatio == 0.5d) List("mdpi", "hdpi", "xhdpi")
      else {
        if(pixelRatio < 1.5d) List("mdpi", "hdpi", "xhdpi")
        else if(pixelRatio < 2d) List("hdpi", "xhdpi", "mdpi")
        else List("xhdpi", "hdpi", "mdpi")
      }
    }

    // Load an image resource which comes from resources for a given dpi.
    // If the path is for drawable-mdpi, the dpi will be mdpi, it's the role
    // of this function to make sure the bitmap is scaled if it needs to.
    private def imageMimeType(resourceName: String): String = html5ResourceExtension(resourceName) match {
      case Some("jpg") => "image/jpeg"
      case Some("jpeg") => "image/jpeg"
      case Some("png") => "image/png"
      case Some("gif") => "image/gif"
      case Some("webp") => "image/webp"
      case Some("svg") => "image/svg+xml"
      case _ => "application/octet-stream"
    }

    private def tryLoadImageDpi(resourceName: String, dpi: String): Loader[Bitmap] = {
      html5ResourceObjectUrl(resourceName, imageMimeType(resourceName)).flatMap { url =>
        val p = new DefaultLoader[Bitmap]()
        val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
        img.addEventListener("load", (_: dom.Event) => {
          val _ = p.success(Html5Bitmap(img, dom.window.devicePixelRatio/dpiToRatio(dpi)))
        })
        img.addEventListener("error", (_: dom.Event) => {
          val _ = p.failure(new RuntimeException(s"image <${html5AssetUrl(resourceName)}> failed to load"))
        })
        img.src = url
        p.loader
      }
    }

    override def loadImage(asset: sgl.assets.DrawableAsset): Loader[Bitmap] = {
      val orderedDensities = bestDPIs(dom.window.devicePixelRatio)
      val orderedVariants = orderedDensities.flatMap(density => asset.variants.find(_.density == density)) ++
        asset.variants.filterNot(variant => orderedDensities.contains(variant.density))
      orderedVariants.foldLeft(
        Loader.failed[Bitmap](new RuntimeException("Found no resources for drawable asset"))
      ){ case (loader, variant) => loader fallbackTo tryLoadImageDpi(variant.resourceName, variant.density) }
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
      private val ctx = canvas.getContext("2d")
      ctx.imageSmoothingEnabled = false;
      val eps = 0.01
      ctx.drawImage(image, eps, eps, image.width-2*eps, image.height-2*eps, 0, 0, canvas.width, canvas.height)
      //ctx.drawImage(image, 0, 0, canvas.width, canvas.height)

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

      override def load(asset: sgl.assets.FontAsset): Loader[Font] = {
        val resourceName = asset.resourceName
        val fontName = fontLock.synchronized {
          fontId += 1
          s"sgl-custom-font-$fontId"
        }

        val fontMimeType = html5ResourceExtension(resourceName) match {
          case Some("ttf") => "font/ttf"
          case Some("otf") => "font/otf"
          case Some("woff") => "font/woff"
          case Some("woff2") => "font/woff2"
          case _ => "application/octet-stream"
        }
        val fontFormat = html5ResourceExtension(resourceName) match {
          case Some("ttf") => "truetype"
          case Some("otf") => "opentype"
          case Some("woff") => "woff"
          case Some("woff2") => "woff2"
          case _ => "opentype"
        }

        val styleNode = dom.document.createElement("style")
        html5ResourceObjectUrl(resourceName, fontMimeType).foreach { url =>
          styleNode.textContent = raw"""
@font-face {
  font-family: '${fontName}';
  src: url('${url}') format("${fontFormat}");
}"""
          dom.document.body.appendChild(styleNode)
        }

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
            val _ = loader.success(Html5Font(fontName, Normal, 10))
          } else {
            val _ = dom.window.setTimeout(() => tryCompleteLoader(), 30)
          }
        }
        val _ = dom.window.setTimeout(() => tryCompleteLoader(), 30)
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
    override val Font: Html5FontCompanion.type = Html5FontCompanion

    type Color = String
    object Html5ColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = s"rgb($r,$g,$b)"
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = {
        val alpha = a/255d
        s"rgba($r,$g,$b,$alpha)"
      }
    }
    override val Color: Html5ColorCompanion.type = Html5ColorCompanion

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
      var width: Float = canvas.width.toFloat
      var height: Float = canvas.height.toFloat
      
      val context = canvas.getContext("2d").asInstanceOf[Ctx2D]
      context.imageSmoothingEnabled = false;

      def resetForFrame(): Unit = {
        context.setTransform(1, 0, 0, 1, 0, 0)
        context.globalAlpha = 1f
        context.imageSmoothingEnabled = false
        this.width = canvas.width.toFloat
        this.height = canvas.height.toFloat
      }

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
        // TODO: not sure if that's the best option, but there are seams sometimes depending on the scaling, and using this
        // tiny epsilon offset seems to help to not accidentally draw from a neighbor pixel in the source image.
        val eps = 0.01
        context.globalAlpha = alpha
        context.drawImage(bitmap.canvas, sx+eps, sy+eps, sw-2*eps, sh-2*eps, dx, dy, dw, dh)
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

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        Html5TextLayout(text, width, context, paint)
      }
    }
    type Canvas = Html5Canvas

    type TextLayout = Html5TextLayout
    case class Html5TextLayout(text: String, layoutWidth: Int, context: Ctx2D, paint: Paint) extends AbstractTextLayout {

      paint.prepareContext(context)
      private val wrapped = TextWrapping.wrap(text, layoutWidth, value => context.measureText(value).width.toFloat)

      override val lines: Vector[String] = wrapped.lines
      val rows: Seq[String] = lines
      override val overflowed: Boolean = wrapped.overflowed
      override val lineCount: Int = lines.size
      override val lineHeight: Int = paint.font.size
      override val ascent: Int = scala.math.ceil(paint.font.size * 0.8).toInt
      override val descent: Int = scala.math.max(0, lineHeight - ascent)
      override val width: Int = scala.math.ceil(lines.foldLeft(0d) { (maximum, line) =>
        scala.math.max(maximum, context.measureText(line).width)
      }).toInt
      override val height: Int = ascent + descent + (lineCount - 1) * lineHeight

      def draw(ctx: Ctx2D, x: Float, y: Float): Unit = {
        paint.prepareContext(ctx)
        var baseline = y + ascent
        lines.foreach { line =>
          val lineX = paint.alignment match {
            case Alignments.Left => x
            case Alignments.Center => x + layoutWidth / 2f
            case Alignments.Right => x + layoutWidth
          }
          ctx.fillText(line, lineX, baseline)
          baseline += lineHeight
        }
      }

    }
  }

  override val Graphics: Html5Graphics.type = Html5Graphics

}
