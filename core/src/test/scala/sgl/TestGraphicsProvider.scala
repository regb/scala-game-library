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

      def width: Float = ???
      def height: Float = ???
      
      override def withSave[A](body: => A): A = ???
      override def translate(x: Float, y: Float): Unit = ???
      override def rotate(theta: Float): Unit = ???
      override def scale(sx: Float, sy: Float): Unit = ???
      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = ???

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f, alpha: Float = 1f): Unit = ???

      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = ???

      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = ???
      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = ???

      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit = ???
      override def drawText(text: TextLayout, x: Float, y: Float): Unit = ???
      override def drawColor(color: Color): Unit = ???
      override def clearRect(x: Float, y: Float, width: Float, height: Float): Unit = ???
      override def renderText(text: String, width: Int, paint: Paint): TextLayout = ???
    }
    type Canvas = TestCanvas
  }
  override val Graphics = new TestGraphics

}
