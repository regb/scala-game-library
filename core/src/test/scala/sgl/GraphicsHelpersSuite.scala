package sgl

import org.scalatest.funsuite.AnyFunSuite

class GraphicsHelperSuite extends AnyFunSuite {

  val graphicsProvider = new TestCanvasProvider with TestSystemProvider {}

  test("BitmapRegion with single bitmap") {
    import graphicsProvider.Graphics._

    val testBitmap = new TestBitmap {
      override def height = 24
      override def width = 32
    }
    val br = BitmapRegion(testBitmap)
    assert(br.bitmap === testBitmap)
    assert(br.x === 0)
    assert(br.y === 0)
    assert(br.width === 32)
    assert(br.height === 24)
  }

  test("BitmapRegion split of a bitmap") {
    import graphicsProvider.Graphics._

    val testBitmap = new TestBitmap {
      override def height = 64
      override def width = 90
    }
    val brs = BitmapRegion.split(testBitmap, 0, 0, 30, 32, 3, 2)

    assert(brs.size === 6)

    assert(brs(0).bitmap === testBitmap)
    assert(brs(0).x === 0)
    assert(brs(0).y === 0)
    assert(brs(0).width === 30)
    assert(brs(0).height === 32)

    assert(brs(1).bitmap === testBitmap)
    assert(brs(1).x === 30)
    assert(brs(1).y === 0)
    assert(brs(1).width === 30)
    assert(brs(1).height === 32)

    assert(brs(2).bitmap === testBitmap)
    assert(brs(2).x === 60)
    assert(brs(2).y === 0)
    assert(brs(2).width === 30)
    assert(brs(2).height === 32)

    assert(brs(3).bitmap === testBitmap)
    assert(brs(3).x === 0)
    assert(brs(3).y === 32)
    assert(brs(3).width === 30)
    assert(brs(3).height === 32)

    assert(brs(4).bitmap === testBitmap)
    assert(brs(4).x === 30)
    assert(brs(4).y === 32)
    assert(brs(4).width === 30)
    assert(brs(4).height === 32)

    assert(brs(5).bitmap === testBitmap)
    assert(brs(5).x === 60)
    assert(brs(5).y === 32)
    assert(brs(5).width === 30)
    assert(brs(5).height === 32)

    val brs2 = BitmapRegion.split(testBitmap, 30, 0, 30, 32, 1, 2)
    assert(brs2.size === 2)
    assert(brs2(0).bitmap === testBitmap)
    assert(brs2(0).x === 30)
    assert(brs2(0).y === 0)
    assert(brs2(0).width === 30)
    assert(brs2(0).height === 32)
    assert(brs2(1).bitmap === testBitmap)
    assert(brs2(1).x === 30)
    assert(brs2(1).y === 32)
    assert(brs2(1).width === 30)
    assert(brs2(1).height === 32)
  }

  test("text wrapping preserves explicit empty lines and trailing newlines") {
    val wrapped = TextWrapping.wrap("one\n\nthree\n", 20, _.length.toFloat)
    assert(wrapped.lines === Vector("one", "", "three", ""))
    assert(!wrapped.overflowed)
  }

  test("text wrapping splits long words at character boundaries") {
    val wrapped = TextWrapping.wrap("abcdefgh", 3, _.length.toFloat)
    assert(wrapped.lines === Vector("abc", "def", "gh"))
    assert(!wrapped.overflowed)

    val oversizedGlyph = TextWrapping.wrap("ab", 1, value => value.length * 2f)
    assert(oversizedGlyph.lines === Vector("a", "b"))
    assert(oversizedGlyph.overflowed)
  }

  test("drawTextBox shrinks, centers, and clips text") {
    import graphicsProvider.Graphics._

    graphicsProvider.frameCanvas.calls.clear()
    val paint = defaultPaint.withFont(Font.Default.withSize(20))
    val layout = graphicsProvider.frameCanvas.drawTextBox(
      "12345678",
      TextBox(5f, 5f, 40f, 30f),
      paint,
      horizontalAlignment = Alignments.Center,
      verticalAlignment = VerticalAlignment.Middle,
      minFontSize = Some(10),
      maxLines = Some(1),
    )

    assert(layout.lineCount === 1)
    assert(layout.lineHeight === 10)
    assert(layout.width === 40)
    assert(graphicsProvider.frameCanvas.calls === Seq(
      "save",
      "clipRect(5.0,5.0,40.0,30.0)",
      "drawText(12345678,5.0,15.0)",
      "restore",
    ))
  }

  test("drawTextBox truncates to maxLines with an ellipsis") {
    import graphicsProvider.Graphics._

    graphicsProvider.frameCanvas.calls.clear()
    val layout = graphicsProvider.frameCanvas.drawTextBox(
      "123456789",
      TextBox(0f, 0f, 20f, 100f),
      defaultPaint.withFont(Font.Default.withSize(10)),
      maxLines = Some(2),
    )

    assert(layout.lines === Seq("1234", "567…"))
    assert(layout.lineCount === 2)
    assert(graphicsProvider.frameCanvas.calls.contains("drawText(1234\n567…,0.0,0.0)"))
  }

  test("TestCanvasProvider loads registered images and records drawing") {
    import graphicsProvider.Graphics._

    val asset = assets.AssetFactory.drawable("sprite.png")
    imageSizes(asset) = (32, 24)
    val bitmap = loadImage(asset).value.get.get

    graphicsProvider.withFrameCanvas { canvas =>
      canvas.drawBitmap(bitmap, 2f, 3f)
    }
    assert(graphicsProvider.frameCanvas.calls.exists(_.startsWith("drawBitmap(2.0,3.0")))

    bitmap.release()
    intercept[IllegalStateException] {
      graphicsProvider.frameCanvas.drawBitmap(bitmap, 0f, 0f)
    }
  }

}
