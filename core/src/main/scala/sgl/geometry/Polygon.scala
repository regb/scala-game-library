package sgl.geometry

class Polygon(val points: Vector[Point]) {

  def nbEdges: Int = points.size

  def edgeStart(i: Int): Point = points(i)
  def edgeEnd(i: Int): Point = points((i+1) % points.size)
}

object Polygon {
  def apply(points: Vector[Point]) = new Polygon(points)
}
