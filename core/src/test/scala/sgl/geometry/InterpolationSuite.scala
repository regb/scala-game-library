package sgl.geometry

import org.scalatest.funsuite.AnyFunSuite

class InterpolationSuite extends AnyFunSuite {

  private val Pi = scala.math.Pi.toFloat

  private def assertClose(actual: Quaternion, expected: Quaternion): Unit = {
    val difference = Seq(actual.x - expected.x, actual.y - expected.y, actual.z - expected.z, actual.w - expected.w)
    assert(difference.forall(d => scala.math.abs(d) < 1e-4f), s"$actual is not close to $expected")
  }

  test("vectors") {
    assert(Vec2(0f, 2f).lerp(Vec2(4f, 4f), 0.5f) == Vec2(2f, 3f))
    assert(Vec3(0f, 2f, 1f).lerp(Vec3(4f, 4f, 1f), 0.5f) == Vec3(2f, 3f, 1f))
    assert(Vec2(0f, 0f).moveTowards(Vec2(10f, 0f), 4f) == Vec2(4f, 0f))
    assert(Vec2(9f, 0f).moveTowards(Vec2(10f, 0f), 4f) == Vec2(10f, 0f))
    assert(Vec2(1f, 1f).moveTowards(Vec2(1f, 1f), 0f) == Vec2(1f, 1f))
    assert(Vec3(0f, 0f, 0f).moveTowards(Vec3(0f, 0f, 10f), 4f) == Vec3(0f, 0f, 4f))
  }

  test("slerp turns at a constant speed") {
    val a = Quaternion.rotationY(0f)
    val b = Quaternion.rotationY(Pi / 2f)
    assertClose(a.slerp(b, 0f), a)
    assertClose(a.slerp(b, 1f), b)
    assertClose(a.slerp(b, 0.25f), Quaternion.rotationY(Pi / 8f))
  }

  test("slerp takes the shortest turn and handles nearly equal rotations") {
    val a = Quaternion.rotationY(0.1f)
    val b = Quaternion.rotationY(0.3f)
    val sameAsB = Quaternion(-b.x, -b.y, -b.z, -b.w)
    assertClose(a.slerp(sameAsB, 0.5f), Quaternion.rotationY(0.2f))
    assertClose(a.slerp(a, 0.5f), a)
    assertClose(a.slerp(Quaternion.rotationY(0.10001f), 0.5f), a)
  }
}
