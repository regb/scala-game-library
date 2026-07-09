package sgl

import org.scalatest.funsuite.AnyFunSuite

class ApplicationSuite extends AnyFunSuite {
  test("frame periods validate FPS and never truncate to zero") {
    assert(Application.framePeriodMillis(60) == 16L)
    assert(Application.framePeriodMillis(2000) == 1L)
    intercept[IllegalArgumentException] {
      Application.framePeriodMillis(0)
    }
  }
}
