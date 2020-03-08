package sgl.geometry

import org.scalatest.FunSuite

class CollisionsSuite extends FunSuite {

  test("polygonWithPolygonSat with simple rects") {
    val p1 = Polygon(Vector(Vec(0,0), Vec(0, 10), Vec(10, 10), Vec(10, 0)))
    val p2 = Polygon(Vector(Vec(5,5), Vec(5, 15), Vec(15, 15), Vec(15, 5)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Vec(15,15), Vec(15, 20), Vec(20, 20), Vec(20, 15)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }

  test("polygonWithPolygonSat with triangles") {
    val p1 = Polygon(Vector(Vec(0,0), Vec(2, 10), Vec(8, 4)))
    val p2 = Polygon(Vector(Vec(4,4), Vec(10, 10), Vec(11, 1)))
    assert(Collisions.polygonWithPolygonSat(p1, p2))
    assert(Collisions.polygonWithPolygonSat(p2, p1))

    val p3 = Polygon(Vector(Vec(12,0), Vec(13, 8), Vec(17, 4.5f)))
    assert(!Collisions.polygonWithPolygonSat(p1, p3))
    assert(!Collisions.polygonWithPolygonSat(p3, p1))
  }
}
