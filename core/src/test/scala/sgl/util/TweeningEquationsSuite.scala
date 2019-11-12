package sgl.util

import TweeningEquations._

import org.scalatest.FunSuite

class TweeningEquationsSuite extends FunSuite {

  test("linear tweening starting and ending values with no or total elapsed time") {
    assert(linear(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(linear(100, 0.5f, 1.5f)(100) === 1.5f)
    val f: (Int) => Double = linear(200, -3, 3)
    assert(f(0) === -3)
    assert(f(200) === 3)
  }
  test("linear tweening of starting and ending values when elapsed time overflow") {
    assert(linear(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(linear(100, 0.5f, 1.5f)(110) === 1.5f)
  }
  test("linear tweening of expected intermediate values") {
    assert(linear(100, 0.5f, 1.5f)(50) === 1.0)
    assert(linear(90, 0, 3)(60) === 2d)
  }
  test("linear tweening all points within bounds") {
    testPointsWithinBounds(linear, 100, -2, 2)
    testPointsWithinBounds(linear, 100, -2, 2)
  }

  test("easeQuad tweening starting and ending values with no or total elapsed time") {
    assert(easeInQuad(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeInQuad(100, 0.5f, 1.5f)(100) === 1.5f)
    assert(easeOutQuad(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeOutQuad(100, 0.5f, 1.5f)(100) === 1.5f)
    assert(easeInOutQuad(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeInOutQuad(100, 0.5f, 1.5f)(100) === 1.5f)
  }
  test("easeQuad tweening of starting and ending values when elapsed time overflow") {
    assert(easeInQuad(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(easeInQuad(100, 0.5f, 1.5f)(110) === 1.5f)
    assert(easeOutQuad(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(easeOutQuad(100, 0.5f, 1.5f)(110) === 1.5f)
    assert(easeInOutQuad(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(easeInOutQuad(100, 0.5f, 1.5f)(110) === 1.5f)
  }
  test("easeQuad tweening of expected intermediate values") {
    assert(easeInQuad(100, 0, 1)(50) === 0.25)
    assert(easeInQuad(100, 0, 100)(50) === 25)
    assert(easeInQuad(100, 100, 200)(50) === 125)
    assert(easeOutQuad(100, 0, 1)(50) === 0.75)
    assert(easeOutQuad(100, 0, 100)(50) === 75)
    assert(easeOutQuad(100, 100, 200)(50) === 175)
    assert(easeInOutQuad(100, 0, 1)(25) === 0.125)
    assert(easeInOutQuad(100, 0, 1)(50) === 0.5f)
    assert(easeInOutQuad(100, 0, 1)(75) === 0.875)
  }
  test("easeQuad tweening all points within bounds") {
    testPointsWithinBounds(easeOutQuad, 100, -2, 2)
    testPointsWithinBounds(easeInQuad, 100, -2, 2)
  }

  test("easeCube tweening starting and ending values with no or total elapsed time") {
    assert(easeInCube(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeInCube(100, 0.5f, 1.5f)(100) === 1.5f)
    assert(easeOutCube(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeOutCube(100, 0.5f, 1.5f)(100) === 1.5f)
  }
  test("easeCube tweening of starting and ending values when elapsed time overflow") {
    assert(easeInCube(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(easeInCube(100, 0.5f, 1.5f)(110) === 1.5f)
    assert(easeOutCube(100, 0.5f, 1.5f)(-5) === 0.5f)
    assert(easeOutCube(100, 0.5f, 1.5f)(110) === 1.5f)
  }
  test("easeCube tweening of expected intermediate values") {
    assert(easeInCube(100, 0, 1)(50) === 0.125)
    assert(easeInCube(100, 0, 100)(50) === 12.5)
    assert(easeInCube(100, 100, 200)(50) === 112.5)
    assert(easeOutCube(100, 0, 1)(50) === 0.875)
    assert(easeOutCube(100, 0, 100)(50) === 87.5)
    assert(easeOutCube(100, 100, 200)(50) === 187.5)
  }
  test("easeCube tweening all points within bounds") {
    testPointsWithinBounds(easeOutCube, 100, -2, 2)
    testPointsWithinBounds(easeInCube, 100, -2, 2)
  }

  test("easeSine tweening starting and ending values with no or total elapsed time") {
    assert(easeOutSine(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeOutSine(100, 0.5f, 1.5f)(100) === 1.5f)
    assert(easeInSine(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeInSine(100, 0.5f, 1.5f)(100) === 1.5f)
  }
  test("easeSine tweening near-end points are close to end value") {
    assert(easeOutSine(100, 0.5f, 1.5f)(99) < 1.5f)
    assert(easeOutSine(100, 0.5f, 1.5f)(99) > 1.4)
    assert(easeOutSine(100, 0.5f, 1.5f)(1) > 0.5f)
    assert(easeOutSine(100, 0.5f, 1.5f)(1) < 0.6)
    assert(easeInSine(100, 0.5f, 1.5f)(99) < 1.5f)
    assert(easeInSine(100, 0.5f, 1.5f)(99) > 1.4)
    assert(easeInSine(100, 0.5f, 1.5f)(1) > 0.5f)
    assert(easeInSine(100, 0.5f, 1.5f)(1) < 0.6)
  }
  test("easeSine tweening intermediate points") {
    assert(easeOutSine(100, 0.5f, 1.5f)(50) > 1)
    assert(easeInSine(100, 0.5f, 1.5f)(50) < 1)
  }
  test("easeSine tweening all points within bounds") {
    testPointsWithinBounds(easeOutSine, 100, -2, 2)
    testPointsWithinBounds(easeInSine, 100, -2, 2)
  }

  test("easeExp tweening starting and ending values with no or total elapsed time") {
    assert(easeInExp(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeInExp(100, 0.5f, 1.5f)(100) === 1.5f)
    assert(easeOutExp(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeOutExp(100, 0.5f, 1.5f)(100) === 1.5f)
  }
  test("easeExp tweening near-end points are close to end value") {
    assert(easeInExp(100, 0.5f, 1.5f)(99) < 1.5f)
    assert(easeInExp(100, 0.5f, 1.5f)(99) > 1.4f)
    assert(easeInExp(100, 0.5f, 1.5f)(1) > 0.5f)
    assert(easeInExp(100, 0.5f, 1.5f)(1) < 0.6f)
    assert(easeOutExp(100, 0.5f, 1.5f)(99) < 1.5f)
    assert(easeOutExp(100, 0.5f, 1.5f)(99) > 1.4f)
    assert(easeOutExp(100, 0.5f, 1.5f)(1) > 0.5f)
    assert(easeOutExp(100, 0.5f, 1.5f)(1) < 0.6f)
  }
  test("easeExp tweening intermediate points") {
    assert(easeInExp(100, 0.5f, 1.5f)(50) < 1)
    assert(easeOutExp(100, 0.5f, 1.5f)(50) > 1)
  }
  test("easeExp tweening all points within bounds") {
    testPointsWithinBounds(easeInExp, 100, -2, 2)
    testPointsWithinBounds(easeOutExp, 100, -2, 2)
  }

  test("easeElastic tweening starting and ending values with no or total elapsed time") {
    assert(easeOutElastic()(100, 0.5f, 1.5f)(0) === 0.5f)
    assert(easeOutElastic()(100, 0.5f, 1.5f)(100) === 1.5f)
  }
  test("easeElastic tweening near-end points are close to end value") {
    assert(easeOutElastic()(100, 0.5f, 1.5f)(1) > 0.5f)
    assert(easeOutElastic()(100, 0.5f, 1.5f)(1) < 0.6f)
    assert(easeOutElastic()(100, 0.5f, 1.5f)(99) < 1.6f)  // can be slightly over.
    assert(easeOutElastic()(100, 0.5f, 1.5f)(99) > 1.4f)
  }

  private def testPointsWithinBounds(f: TweeningFunction, duration: Int, start: Float, end: Float): Unit = {
    val fn = f(duration, start, end)
    for(t <- 0 to duration) {
      assert(fn(t) >= start && fn(t) <= end)
    }
  }
}
