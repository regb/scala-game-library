package sgl

import sgl.util.Loader

// TODO: we should add some internal state, which will make this useable for
// mocking and testing that expected functions have been called. Maybe we can
// have a true pixel array and have functions to query internal state.

trait TestGraphicsProvider extends GraphicsProvider {
  this: SystemProvider =>

  class TestGraphics extends Graphics {

    class TestBitmap extends AbstractBitmap {
      override def height: Int = ???
      override def width: Int = ???

      override def release(): Unit = {}
    }
    type Bitmap = TestBitmap
    override def loadImage(path: ResourcePath): Loader[Bitmap] = ???

    class TestFont extends AbstractFont {
      override def withSize(size: Int): Font = ???
      override def withStyle(style: Font.Style): Font = ???
      override def isBold(): Boolean = ???
      override def isItalic(): Boolean = ???
    }
    type Font = TestFont
    class TestFontCompanion extends FontCompanion {
      def create(family: String, style: Style, size: Int): Font = ???
      def load(path: ResourcePath): Loader[Font] = ???
      val Default: Font = new TestFont
      val DefaultBold: Font = new TestFont
      val Monospace: Font = new TestFont
      val SansSerif: Font = new TestFont
      val Serif: Font = new TestFont
    }
    val Font = new TestFontCompanion

    type Color = Int
    class TestColorCompanion extends ColorCompanion {
      def rgb(r: Int, g: Int, b: Int): Color = ???
      def rgba(r: Int, g: Int, b: Int, a: Int): Color = ???
    }
    val Color = new TestColorCompanion

    class TestPaint extends AbstractPaint {
      def font: Font = ???
      def withFont(font: Font): Paint = ???
      def color: Color = ???
      def withColor(color: Color): Paint = ???
      def alignment: Alignments.Alignment = ???
      def withAlignment(alignment: Alignments.Alignment): Paint = ???
    }
    type Paint = TestPaint
    def defaultPaint: Paint = ???

    class TestTextLayout extends AbstractTextLayout {
      def height: Int = ???
    }
    type TextLayout = TestTextLayout

    class TestCanvas extends AbstractCanvas {

      def width: Int = ???
      def height: Int = ???
      
      def withSave[A](body: => A): A = ???
      def translate(x: Int, y: Int): Unit = ???
      def rotate(theta: Double): Unit = ???
      def scale(sx: Double, sy: Double): Unit = ???
      def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = ???

      def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = ???

      def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f, alpha: Float = 1f): Unit = ???

      def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = ???

      def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = ???
      def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = ???

      def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = ???
      def drawText(text: TextLayout, x: Int, y: Int): Unit = ???
      def drawColor(color: Color): Unit = ???
      def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = ???
      def renderText(text: String, width: Int, paint: Paint): TextLayout = ???
    }
    type Canvas = TestCanvas
  }
  override val Graphics = new TestGraphics

}
