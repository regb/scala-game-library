package sgl.util

import TweeningEquations._

import org.scalatest.FunSuite

class TweeningEquationsSuite extends FunSuite {

  test("linear tweening starting and ending values with no or total elapsed time") {
    assert(linear(100, 0.5, 1.5)(0) === 0.5)
    assert(linear(100, 0.5, 1.5)(100) === 1.5)
    val f: (Int) => Double = linear(200, -3, 3)
    assert(f(0) === -3)
    assert(f(200) === 3)
  }
  test("linear tweening of starting and ending values when elapsed time overflow") {
    assert(linear(100, 0.5, 1.5)(-5) === 0.5)
    assert(linear(100, 0.5, 1.5)(110) === 1.5)
  }
  test("linear tweening of expected intermediate values") {
    assert(linear(100, 0.5, 1.5)(50) === 1.0)
    assert(linear(90, 0, 3)(60) === 2d)
  }

  test("easeQuad tweening starting and ending values with no or total elapsed time") {
    assert(easeInQuad(100, 0.5, 1.5)(0) === 0.5)
    assert(easeInQuad(100, 0.5, 1.5)(100) === 1.5)
    assert(easeOutQuad(100, 0.5, 1.5)(0) === 0.5)
    assert(easeOutQuad(100, 0.5, 1.5)(100) === 1.5)
    assert(easeInOutQuad(100, 0.5, 1.5)(0) === 0.5)
    assert(easeInOutQuad(100, 0.5, 1.5)(100) === 1.5)
  }
  test("easeQuad tweening of starting and ending values when elapsed time overflow") {
    assert(easeInQuad(100, 0.5, 1.5)(-5) === 0.5)
    assert(easeInQuad(100, 0.5, 1.5)(110) === 1.5)
    assert(easeOutQuad(100, 0.5, 1.5)(-5) === 0.5)
    assert(easeOutQuad(100, 0.5, 1.5)(110) === 1.5)
    assert(easeInOutQuad(100, 0.5, 1.5)(-5) === 0.5)
    assert(easeInOutQuad(100, 0.5, 1.5)(110) === 1.5)
  }
  test("easeQuad tweening of expected intermediate values") {
    assert(easeInQuad(100, 0, 1)(50) === 0.25)
    assert(easeInQuad(100, 0, 100)(50) === 25)
    assert(easeInQuad(100, 100, 200)(50) === 125)
    assert(easeOutQuad(100, 0, 1)(50) === 0.75)
    assert(easeOutQuad(100, 0, 100)(50) === 75)
    assert(easeOutQuad(100, 100, 200)(50) === 175)
    assert(easeInOutQuad(100, 0, 1)(25) === 0.125)
    assert(easeInOutQuad(100, 0, 1)(50) === 0.5)
    assert(easeInOutQuad(100, 0, 1)(75) === 0.875)
  }

  test("easeCube tweening starting and ending values with no or total elapsed time") {
    assert(easeInCube(100, 0.5, 1.5)(0) === 0.5)
    assert(easeInCube(100, 0.5, 1.5)(100) === 1.5)
    assert(easeOutCube(100, 0.5, 1.5)(0) === 0.5)
    assert(easeOutCube(100, 0.5, 1.5)(100) === 1.5)
  }
  test("easeCube tweening of starting and ending values when elapsed time overflow") {
    assert(easeInCube(100, 0.5, 1.5)(-5) === 0.5)
    assert(easeInCube(100, 0.5, 1.5)(110) === 1.5)
    assert(easeOutCube(100, 0.5, 1.5)(-5) === 0.5)
    assert(easeOutCube(100, 0.5, 1.5)(110) === 1.5)
  }
  test("easeCube tweening of expected intermediate values") {
    assert(easeInCube(100, 0, 1)(50) === 0.125)
    assert(easeInCube(100, 0, 100)(50) === 12.5)
    assert(easeInCube(100, 100, 200)(50) === 112.5)
    assert(easeOutCube(100, 0, 1)(50) === 0.875)
    assert(easeOutCube(100, 0, 100)(50) === 87.5)
    assert(easeOutCube(100, 100, 200)(50) === 187.5)
  }
}
