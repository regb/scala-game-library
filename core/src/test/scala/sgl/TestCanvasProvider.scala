package sgl

import scala.collection.mutable

import sgl.assets.{DrawableAsset, FontAsset}
import sgl.util.Loader

/** Deterministic in-memory Canvas implementation for tests.
  *
  * It does not rasterize pixels. Draw and transform operations are recorded in
  * [[TestCanvas.calls]], making it suitable for assertions without a window.
  */
trait TestCanvasProvider extends CanvasProvider {
  this: SystemProvider =>

  class TestGraphics extends Graphics {

    class TestBitmap(initialWidth: Int = 0, initialHeight: Int = 0) extends AbstractBitmap {
      private var releasedValue = false

      override def width: Int = initialWidth
      override def height: Int = initialHeight
      def isReleased: Boolean = releasedValue
      override def release(): Unit = releasedValue = true
    }
    type Bitmap = TestBitmap

    val imageSizes: mutable.Map[DrawableAsset, (Int, Int)] = mutable.Map.empty

    override def loadImage(asset: DrawableAsset): Loader[Bitmap] =
      imageSizes.get(asset) match {
        case Some((width, height)) => Loader.successful(new TestBitmap(width, height))
        case None => Loader.failed(new NoSuchElementException(s"No test image registered: $asset"))
      }

    final class TestFont(
      val family: String,
      override val size: Int,
      override val isBold: Boolean,
      override val isItalic: Boolean,
    ) extends AbstractFont {
      override def withSize(size: Int): Font = new TestFont(family, size, isBold, isItalic)

      override def withStyle(style: Font.Style): Font = style match {
        case Font.Bold => new TestFont(family, size, isBold = true, isItalic = false)
        case Font.BoldItalic => new TestFont(family, size, isBold = true, isItalic = true)
        case Font.Italic => new TestFont(family, size, isBold = false, isItalic = true)
        case Font.Normal => new TestFont(family, size, isBold = false, isItalic = false)
      }
    }
    type Font = TestFont

    class TestFontCompanion extends FontCompanion {
      override def create(family: String, style: Style, size: Int): Font =
        new TestFont(family, size, style == Bold || style == BoldItalic, style == Italic || style == BoldItalic)

      override def load(asset: FontAsset): Loader[Font] =
        Loader.successful(new TestFont(asset.resourceName, 12, isBold = false, isItalic = false))

      override val Default: Font = create("default", Normal, 12)
      override val DefaultBold: Font = create("default", Bold, 12)
      override val Monospace: Font = create("monospace", Normal, 12)
      override val SansSerif: Font = create("sans-serif", Normal, 12)
      override val Serif: Font = create("serif", Normal, 12)
    }
    override val Font: TestFontCompanion = new TestFontCompanion

    type Color = Int
    class TestColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = rgba(r, g, b, 255)
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color =
        ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff)
    }
    override val Color: TestColorCompanion = new TestColorCompanion

    final class TestPaint(
      val font: Font,
      val color: Color,
      val alignment: Alignments.Alignment,
    ) extends AbstractPaint {
      override def withFont(font: Font): Paint = new TestPaint(font, color, alignment)
      override def withColor(color: Color): Paint = new TestPaint(font, color, alignment)
      override def withAlignment(alignment: Alignments.Alignment): Paint = new TestPaint(font, color, alignment)
    }
    type Paint = TestPaint
    override def defaultPaint: Paint = new TestPaint(Font.Default, Color.Black, Alignments.Left)

    final class TestTextLayout(val text: String, val layoutWidth: Int, val paint: Paint) extends AbstractTextLayout {
      private def measure(value: String): Float = value.length * paint.font.size / 2f
      private val wrapped = TextWrapping.wrap(text, layoutWidth, measure)
      override val lines: Vector[String] = wrapped.lines
      override val overflowed: Boolean = wrapped.overflowed
      override val lineCount: Int = lines.size
      override val lineHeight: Int = paint.font.size
      override val ascent: Int = scala.math.ceil(lineHeight * 0.8).toInt
      override val descent: Int = lineHeight - ascent
      override val width: Int = scala.math.ceil(lines.foldLeft(0f)((maximum, line) => scala.math.max(maximum, measure(line)))).toInt
      override val height: Int = ascent + descent + (lineCount - 1) * lineHeight
    }
    type TextLayout = TestTextLayout

    class TestCanvas extends AbstractCanvas {
      val calls: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty

      override def withSave[A](body: => A): A = {
        calls += "save"
        try body
        finally calls += "restore"
      }

      override def translate(x: Float, y: Float): Unit = calls += s"translate($x,$y)"
      override def rotate(theta: Float): Unit = calls += s"rotate($theta)"
      override def scale(sx: Float, sy: Float): Unit = calls += s"scale($sx,$sy)"
      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = calls += s"clipRect($x,$y,$width,$height)"

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit = {
        if(bitmap.isReleased) throw new IllegalStateException("Trying to draw a released test bitmap")
        calls += s"drawBitmap($dx,$dy,$dw,$dh,$sx,$sy,$sw,$sh,$alpha)"
      }

      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit =
        calls += s"drawRect($x,$y,$width,$height)"

      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit =
        calls += s"drawOval($x,$y,$width,$height)"

      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit =
        calls += s"drawLine($x1,$y1,$x2,$y2)"

      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit =
        calls += s"drawString($str,$x,$y)"

      override def drawText(text: TextLayout, x: Float, y: Float): Unit =
        calls += s"drawText(${text.text},$x,$y)"

      override def renderText(text: String, width: Int, paint: Paint): TextLayout =
        new TestTextLayout(text, width, paint)
    }
    type Canvas = TestCanvas
  }

  override val Graphics: TestGraphics = new TestGraphics
  val frameCanvas: Graphics.TestCanvas = new Graphics.TestCanvas

  override def withFrameCanvas[A](f: Graphics.Canvas => A): A = f(frameCanvas)
}
