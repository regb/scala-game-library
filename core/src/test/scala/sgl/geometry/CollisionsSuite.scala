package sgl.geometry

import org.scalatest.FunSuite

class CollisionsSuite extends FunSuite {

  test("polygonWithPolygonSat with simple rects") {
    val p1 = Polygon(Vector(Point(0,0), Point(0, 10), Point(10, 10), Point(10, 0)))
    val p2 = Polygon(Vector(Point(5,5), Point(5, 15), Point(15, 15), Point(15, 5)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Point(15,15), Point(15, 20), Point(20, 20), Point(20, 15)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }

  test("polygonWithPolygonSat with triangles") {
    val p1 = Polygon(Vector(Point(0,0), Point(2, 10), Point(8, 4)))
    val p2 = Polygon(Vector(Point(4,4), Point(10, 10), Point(11, 1)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Point(12,0), Point(13, 8), Point(17, 4.5f)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }
}
