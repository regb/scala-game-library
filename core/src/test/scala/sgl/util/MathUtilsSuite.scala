package sgl.util

import org.scalatest.funsuite.AnyFunSuite

import MathUtils._

class MathUtilsSuite extends AnyFunSuite {

  private val Pi = scala.math.Pi.toFloat

  private def assertClose(actual: Float, expected: Float): Unit =
    assert(scala.math.abs(actual - expected) < 1e-4f, s"$actual is not close to $expected")

  test("clamp") {
    assert(clamp(5, 0, 3) == 3)
    assert(clamp(-1f, 0f, 3f) == 0f)
    assert(clamp(2.0, 0.0, 3.0) == 2.0)
  }

  test("lerp is exact at both ends and extrapolates") {
    assert(lerp(0.1f, 0.7f, 0f) == 0.1f)
    assert(lerp(0.1f, 0.7f, 1f) == 0.7f)
    assertClose(lerp(10f, 20f, 0.25f), 12.5f)
    assertClose(lerp(10f, 20f, 2f), 30f)
    assert(lerp(1.0, 3.0, 0.5) == 2.0)
  }

  test("inverseLerp and remap") {
    assertClose(inverseLerp(10f, 20f, 12.5f), 0.25f)
    assert(inverseLerp(3f, 3f, 5f) == 0f)
    assertClose(remap(5f, 0f, 10f, 100f, 200f), 150f)
    assertClose(remap(5f, 0f, 10f, 200f, 100f), 150f)
  }

  test("smoothstep") {
    assert(smoothstep(0f, 1f, -1f) == 0f)
    assert(smoothstep(0f, 1f, 2f) == 1f)
    assertClose(smoothstep(0f, 1f, 0.5f), 0.5f)
    assert(smoothstep(0f, 1f, 0.25f) < 0.25f)
    assert(smoothstep(1f, 1f, 0.5f) == 0f && smoothstep(1f, 1f, 1f) == 1f)
  }

  test("moveTowards does not pass the target") {
    assert(moveTowards(0f, 10f, 3f) == 3f)
    assert(moveTowards(0f, -10f, 3f) == -3f)
    assert(moveTowards(9f, 10f, 3f) == 10f)
  }

  test("damp does not depend on the frame rate") {
    val oneStep = damp(0f, 10f, 4f, 0.5f)
    val manySteps = (0 until 50).foldLeft(0f)((x, _) => damp(x, 10f, 4f, 0.01f))
    assert(scala.math.abs(oneStep - manySteps) < 1e-2f)
    assert(oneStep > 0f && oneStep < 10f)
  }

  test("wrap") {
    assertClose(wrap(370f, 0f, 360f), 10f)
    assertClose(wrap(-10f, 0f, 360f), 350f)
    assert(wrap(360f, 0f, 360f) == 0f)
    assertClose(wrap(-750f, 0f, 360f), 330f)
    val tiny = wrap(-1e-9f, 0f, 360f)
    assert(tiny >= 0f && tiny < 360f)
  }

  test("angles take the shortest turn") {
    assertClose(wrapAngle(3f * Pi / 2f), -Pi / 2f)
    assertClose(deltaAngle(Pi - 0.1f, -Pi + 0.1f), 0.2f)
    assertClose(deltaAngle(-Pi + 0.1f, Pi - 0.1f), -0.2f)
    assertClose(wrapAngle(lerpAngle(Pi - 0.1f, -Pi + 0.2f, 0.5f)), -Pi + 0.05f)
    assertClose(moveTowardsAngle(Pi - 0.1f, -Pi + 0.1f, 0.05f), Pi - 0.05f)
    assert(moveTowardsAngle(0f, 0.1f, 1f) == 0.1f)
  }
}
