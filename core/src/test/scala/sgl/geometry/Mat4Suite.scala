package sgl.geometry

import org.scalatest.funsuite.AnyFunSuite

class Mat4Suite extends AnyFunSuite {

  private def assertClose(actual: Mat4, expected: Mat4): Unit =
    actual.values.zip(expected.values).zipWithIndex.foreach { case ((a, e), i) =>
      assert(scala.math.abs(a - e) < 1e-4f, s"at index $i: $a was not close to $e")
    }

  test("a matrix times its inverse is the identity") {
    val matrix = Mat4.trs(
      Vec3(3f, -2f, 7f),
      Quaternion.axisAngle(Vec3(1f, 2f, 3f), 0.8f),
      Vec3(2f, 0.5f, 3f)
    )

    assertClose(matrix * matrix.inverse, Mat4.Identity)
    assertClose(matrix.inverse * matrix, Mat4.Identity)
    assertClose(Mat4.perspective(1f, 1.5f, 0.1f, 100f).inverse.inverse, Mat4.perspective(1f, 1.5f, 0.1f, 100f))
  }

  test("a matrix that flattens space has no inverse") {
    intercept[ArithmeticException] {
      Mat4.scale(1f, 0f, 1f).inverse
    }
  }

  test("lookAlong orients -Z along the direction, and its inverse is the lookAt view") {
    val cases = Vector(
      (Vec3(0f, 0f, 5f), Vec3(0f, 0f, 0f), Vec3.UnitY),
      (Vec3(4f, 2f, -3f), Vec3(-1f, 0.5f, 2f), Vec3.UnitY),
      (Vec3(1f, 1f, 1f), Vec3(1f, 5f, 1.2f), Vec3.UnitZ),
      (Vec3(0f, 0f, 0f), Vec3(-3f, -1f, 0f), Vec3.UnitZ),
      (Vec3(2f, 0f, 0f), Vec3(2f, 0.1f, -4f), Vec3.UnitY)
    )
    cases.foreach { case (eye, target, up) =>
      val rotation = Quaternion.lookAlong(target - eye, up)
      val camera = Mat4.trs(eye, rotation, Vec3.One)
      assertClose(camera.inverse, Mat4.lookAt(eye, target, up))
    }
  }
}
