package sgl.geometry

import org.scalatest.funsuite.AnyFunSuite

class RectSuite extends AnyFunSuite {

  test("Create rectangle with correct coordinates, dimensions, and center.") {
    val r1 = Rect(4, 7, 10, 20)
    assert(r1.left === 4)
    assert(r1.top === 7)
    assert(r1.width === 10)
    assert(r1.height === 20)
    assert(r1.right === 14)
    assert(r1.bottom === 27)
    assert(r1.centerX === 9)
    assert(r1.centerY === 17)
  }

  test("Rectangle intesects with a point") {
    val r1 = Rect(0, 0, 10, 20)
    assert(r1.intersect(2, 3))
    assert(r1.intersect(5, 15))
    assert(!r1.intersect(11, 10))
    assert(!r1.intersect(5, 25))
  }
}
