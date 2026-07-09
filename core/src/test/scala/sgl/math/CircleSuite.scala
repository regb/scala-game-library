package sgl.math

import org.scalatest.funsuite.AnyFunSuite

class CircleSuite extends AnyFunSuite {

  test("adds a Vec2") {
    val c = Circle(0, 0, 10)
    val v = Vec2(1, 1)
    val expected = Circle(1,1,10)
    assert(c + v === expected)
  }

  test("subtracts a Vec2") {
    val c = Circle(0, 0, 10)
    val v = Vec2(1, 1)
    val expected = Circle(-1,-1,10)
    assert(c - v === expected)
  }
}
