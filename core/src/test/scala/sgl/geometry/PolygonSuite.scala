package sgl.geometry

import org.scalatest.FunSuite

class PolygonSuite extends FunSuite {

  test("Create polygon with correct vertices and edges") {
    val p = Polygon(Vector(Vec(0,0), Vec(1, 10), Vec(5, 5)))
    assert(p.vertices(0) === Vec(0,0))
    assert(p.vertices(1) === Vec(1,10))
    assert(p.vertices(2) === Vec(5,5))
    assert(p.nbEdges === 3)
    assert(p.edgeStart(0) === Vec(0,0))
    assert(p.edgeEnd(0) === Vec(1,10))
    assert(p.edgeStart(1) === Vec(1,10))
    assert(p.edgeEnd(1) === Vec(5,5))
    assert(p.edgeStart(2) === Vec(5,5))
    assert(p.edgeEnd(2) === Vec(0,0))
  }

  test("Correct bounding box for polygon") {
    val p = Polygon(Vector(Vec(0, 5), Vec(3, 8), Vec(7, 4), Vec(4, -2)))
    val bb = p.boundingBox
    assert(bb.top == -2)
    assert(bb.left == 0)
    assert(bb.right == 7)
    assert(bb.bottom == 8)
  }

}
