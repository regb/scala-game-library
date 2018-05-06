package sgl.util

import TweeningEquations._

import org.scalatest.FunSuite

class TweeningEquationsSuite extends FunSuite {

  test("linear tweening starting and ending values with no or total elapsed time") {
    assert(linear(0, 0.5, 1.5, 100) === 0.5)
    assert(linear(100, 0.5, 1.5, 100) === 1.5)
  }

  test("linear tweening of starting and ending values when elapsed time overflow") {
    assert(linear(-5, 0.5, 1.5, 100) === 0.5)
    assert(linear(110, 0.5, 1.5, 100) === 1.5)
  }

  test("linear tweening of expected intermediate values") {
    assert(linear(50, 0.5, 1.5, 100) === 1.0)
    assert(linear(60, 0, 3, 90) === 2d)
  }

}
