package sgl.math

import org.scalatest.funsuite.AnyFunSuite

class EllipseSuite extends AnyFunSuite {

  test("adds a Vec2") {
    val c = Ellipse(0, 0, 10, 20)
    val v = Vec2(1, 1)
    val expected = Ellipse(1,1,10, 20)
    assert(c + v === expected)
  }

  test("subtracts a Vec2") {
    val c = Ellipse(0, 0, 10, 20)
    val v = Vec2(1, 1)
    val expected = Ellipse(-1,-1,10, 20)
    assert(c - v === expected)
  }
}
