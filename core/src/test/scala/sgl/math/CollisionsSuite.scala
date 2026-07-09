package sgl.math

import org.scalatest.funsuite.AnyFunSuite

class CollisionsSuite extends AnyFunSuite {

  test("polygonWithPolygonSat with simple rects") {
    val p1 = Polygon(Vector(Vec2(0,0), Vec2(0, 10), Vec2(10, 10), Vec2(10, 0)))
    val p2 = Polygon(Vector(Vec2(5,5), Vec2(5, 15), Vec2(15, 15), Vec2(15, 5)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Vec2(15,15), Vec2(15, 20), Vec2(20, 20), Vec2(20, 15)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }

  test("polygonWithPolygonSat with triangles") {
    val p1 = Polygon(Vector(Vec2(0,0), Vec2(2, 10), Vec2(8, 4)))
    val p2 = Polygon(Vector(Vec2(4,4), Vec2(10, 10), Vec2(11, 1)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Vec2(12,0), Vec2(13, 8), Vec2(17, 4.5f)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }
}
