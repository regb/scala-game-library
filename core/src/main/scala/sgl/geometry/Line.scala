package sgl.geometry

case class Line(xa: Float, ya: Float, xb: Float, yb: Float) {

  def a: Point = Point(xa, ya)
  def b: Point = Point(xb, yb)

}
