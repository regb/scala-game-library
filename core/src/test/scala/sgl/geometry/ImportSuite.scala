package importing

import _root_.sgl._

import org.scalatest.funsuite.AnyFunSuite

class ImportSuite extends AnyFunSuite {

  test("importing sgl._ does not hide scala.math") {
    assert(math.abs(-1f) == 1f)
    assert(math.max(1, 2) == 2)
    assert(math.sin(0.0) == 0.0)
  }

  test("importing sgl._ brings the geometry package") {
    assert(geometry.Vec2(1f, 2f) + geometry.Vec2(1f, 0f) == geometry.Vec2(2f, 2f))
  }
}
