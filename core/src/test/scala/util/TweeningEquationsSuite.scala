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

  test("easeSine tweening starting and ending values with no or total elapsed time") {
    assert(easeOutSine(100, 0.5, 1.5)(0) === 0.5)
    assert(easeOutSine(100, 0.5, 1.5)(100) === 1.5)
    assert(easeInSine(100, 0.5, 1.5)(0) === 0.5)
    assert(easeInSine(100, 0.5, 1.5)(100) === 1.5)
  }
  test("easeSine tweening near-end points are close to end value") {
    assert(easeOutSine(100, 0.5, 1.5)(99) < 1.5)
    assert(easeOutSine(100, 0.5, 1.5)(99) > 1.4)
    assert(easeOutSine(100, 0.5, 1.5)(1) > 0.5)
    assert(easeOutSine(100, 0.5, 1.5)(1) < 0.6)
    assert(easeInSine(100, 0.5, 1.5)(99) < 1.5)
    assert(easeInSine(100, 0.5, 1.5)(99) > 1.4)
    assert(easeInSine(100, 0.5, 1.5)(1) > 0.5)
    assert(easeInSine(100, 0.5, 1.5)(1) < 0.6)
  }
  test("easeSine tweening intermediate points") {
    assert(easeOutSine(100, 0.5, 1.5)(50) > 1)
    assert(easeInSine(100, 0.5, 1.5)(50) < 1)
  }

  test("easeExp tweening starting and ending values with no or total elapsed time") {
    assert(easeInExp(100, 0.5, 1.5)(0) === 0.5)
    assert(easeInExp(100, 0.5, 1.5)(100) === 1.5)
    assert(easeOutExp(100, 0.5, 1.5)(0) === 0.5)
    assert(easeOutExp(100, 0.5, 1.5)(100) === 1.5)
  }
  test("easeExp tweening near-end points are close to end value") {
    assert(easeInExp(100, 0.5, 1.5)(99) < 1.5)
    assert(easeInExp(100, 0.5, 1.5)(99) > 1.4)
    assert(easeInExp(100, 0.5, 1.5)(1) > 0.5)
    assert(easeInExp(100, 0.5, 1.5)(1) < 0.6)
    assert(easeOutExp(100, 0.5, 1.5)(99) < 1.5)
    assert(easeOutExp(100, 0.5, 1.5)(99) > 1.4)
    assert(easeOutExp(100, 0.5, 1.5)(1) > 0.5)
    assert(easeOutExp(100, 0.5, 1.5)(1) < 0.6)
  }
  test("easeExp tweening intermediate points") {
    assert(easeInExp(100, 0.5, 1.5)(50) < 1)
    assert(easeOutExp(100, 0.5, 1.5)(50) > 1)
  }

  test("easeElastic tweening starting and ending values with no or total elapsed time") {
    assert(easeOutElastic()(100, 0.5, 1.5)(0) === 0.5)
    assert(easeOutElastic()(100, 0.5, 1.5)(100) === 1.5)
  }
  test("easeElastic tweening near-end points are close to end value") {
    assert(easeOutElastic()(100, 0.5, 1.5)(1) > 0.5)
    assert(easeOutElastic()(100, 0.5, 1.5)(1) < 0.6)
    assert(easeOutElastic()(100, 0.5, 1.5)(99) < 1.6)  // can be slightly over.
    assert(easeOutElastic()(100, 0.5, 1.5)(99) > 1.4)
  }

  // TODO: general test that covers many points and ensures they are all within bounds
  //       to apply to all but the few easing that go out of bounds sometimes.
}
