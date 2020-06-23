package sgl.geometry

import org.scalatest.FunSuite

class EllipseSuite extends FunSuite {

  test("adds a Vec") {
    val c = Ellipse(0, 0, 10, 20)
    val v = Vec(1, 1)
    val expected = Ellipse(1,1,10, 20)
    assert(c + v === expected)
  }

  test("subtracts a Vec") {
    val c = Ellipse(0, 0, 10, 20)
    val v = Vec(1, 1)
    val expected = Ellipse(-1,-1,10, 20)
    assert(c - v === expected)
  }
}
