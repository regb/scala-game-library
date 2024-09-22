package sgl
package proxy

import sgl.util._


trait ProxyGraphicsProvider extends GraphicsProvider {
  this: ProxySystemProvider =>

  val PlatformProxy: PlatformProxy

  object ProxyGraphics extends Graphics {

    override def loadImage(path: ResourcePath): Loader[Bitmap] = PlatformProxy.graphicsProxy.loadImage(path.path).map(b => ProxyBitmap(b))

    case class ProxyBitmap(bitmap: BitmapProxy) extends AbstractBitmap {
      override def height: Int = bitmap.height
      override def width: Int = bitmap.width
      override def release(): Unit = bitmap.release()
    }
    type Bitmap = ProxyBitmap

    case class ProxyFont(font: FontProxy) extends AbstractFont {
      override def withSize(s: Int): Font = ProxyFont(font.withSize(s))
      override def withStyle(s: Font.Style): Font = ProxyFont(font.withStyle(ProxyFontCompanion.fromStyle(s)))
      override def size: Int = font.size
      override def isBold: Boolean = font.isBold
      override def isItalic: Boolean = font.isItalic
    }
    type Font = ProxyFont

    object ProxyFontCompanion extends FontCompanion {
      override def create(family: String, style: Style, size: Int): Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.create(family, fromStyle(style), size))
      override def load(path: ResourcePath): Loader[Font] = PlatformProxy.graphicsProxy.fontCompanionProxy.load(path.path).map(f => ProxyFont(f))
      override def Default: Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.Default)
      override def DefaultBold: Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.DefaultBold)
      override def Monospace: Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.Monospace)
      override def SansSerif: Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.SansSerif)
      override def Serif: Font = ProxyFont(PlatformProxy.graphicsProxy.fontCompanionProxy.Serif)

      def toStyle(s: FontProxy.Style): Style = s match {
        case FontProxy.Bold => Bold
        case FontProxy.BoldItalic => BoldItalic
        case FontProxy.Italic => Italic
        case FontProxy.Normal => Normal
      }
      def fromStyle(s: Style): FontProxy.Style = s match {
        case Bold => FontProxy.Bold
        case BoldItalic => FontProxy.BoldItalic
        case Italic => FontProxy.Italic
        case Normal => FontProxy.Normal
      }
    }
    override val Font = ProxyFontCompanion

    type Color = ColorProxy
    object ProxyColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = PlatformProxy.graphicsProxy.colorCompanionProxy.rgb(r, g, b)
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = PlatformProxy.graphicsProxy.colorCompanionProxy.rgba(r, g, b, a)
    }
    override val Color = ProxyColorCompanion

    case class ProxyPaint(paint: PaintProxy) extends AbstractPaint {
      override val font = ProxyFont(paint.font)
      override def withFont(f: Font) = ProxyPaint(paint.withFont(f.font))

      override val color = paint.color
      override def withColor(c: Color) = ProxyPaint(paint.withColor(c))

      override val alignment = fromProxyAlignment(paint.alignment)
      override def withAlignment(a: Alignments.Alignment) = ProxyPaint(paint.withAlignment(toProxyAlignment(a)))
    }

    def toProxyAlignment(alignment: Alignments.Alignment): AlignmentsProxy.Alignment = alignment match {
      case Alignments.Left => AlignmentsProxy.Left
      case Alignments.Center => AlignmentsProxy.Center
      case Alignments.Right => AlignmentsProxy.Right
    }
    def fromProxyAlignment(alignment: AlignmentsProxy.Alignment): Alignments.Alignment = alignment match {
      case AlignmentsProxy.Left => Alignments.Left
      case AlignmentsProxy.Center => Alignments.Center
      case AlignmentsProxy.Right => Alignments.Right
    }

    type Paint = ProxyPaint

    override def defaultPaint: Paint = ProxyPaint(PlatformProxy.graphicsProxy.defaultPaint)

    case class ProxyTextLayout(textLayout: TextLayoutProxy) extends AbstractTextLayout {
      override def height: Int = textLayout.height
    }
    type TextLayout = ProxyTextLayout

    case class ProxyCanvas(canvas: CanvasProxy) extends AbstractCanvas {
      override def withSave[A](body: => A): A = canvas.withSave(body)
      override def translate(x: Float, y: Float): Unit = canvas.translate(x,y)
      override def rotate(theta: Float): Unit = canvas.rotate(theta)
      override def scale(sx: Float, sy: Float): Unit = canvas.scale(sx,sy)
      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = canvas.clipRect(x,y,width,height)
      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit =
        canvas.drawBitmap(bitmap.bitmap, dx, dy, dw, dh, sx, sy, sw, sh, alpha)
      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = canvas.drawLine(x1, y1, x2, y2, paint.paint)
      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = canvas.drawRect(x, y, width, height, paint.paint)
      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = canvas.drawOval(x,y,width, height, paint.paint)
      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit = canvas.drawString(str,x,y,paint.paint)
      override def drawText(text: TextLayout, x: Float, y: Float): Unit = canvas.drawText(text.textLayout, x, y)
      override def renderText(text: String, width: Int, paint: Paint): TextLayout = ProxyTextLayout(canvas.renderText(text, width, paint.paint))
    }
    type Canvas = ProxyCanvas

  }
  override val Graphics = ProxyGraphics

}
