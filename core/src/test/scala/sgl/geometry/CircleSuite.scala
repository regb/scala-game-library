package sgl.geometry

import org.scalatest.FunSuite

class CircleSuite extends FunSuite {

  test("adds a Vec") {
    val c = Circle(0, 0, 10)
    val v = Vec(1, 1)
    val expected = Circle(1,1,10)
    assert(c + v === expected)
  }

  test("subtracts a Vec") {
    val c = Circle(0, 0, 10)
    val v = Vec(1, 1)
    val expected = Circle(-1,-1,10)
    assert(c - v === expected)
  }
}
