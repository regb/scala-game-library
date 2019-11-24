package sgl

import org.scalatest.FunSuite

class GraphicsHelperSuite extends FunSuite {

  val graphicsProvider = new TestGraphicsProvider with TestSystemProvider {}

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

}
