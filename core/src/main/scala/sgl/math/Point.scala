package sgl.math

import scala.language.implicitConversions

/*
 * Point and Vec2 are floats as they are used to simulate physics
 * and using Int (pixels) tend to lose precision when a frame only
 * move a fraction of a pixel.
 */

case class Point(var x: Float, var y: Float) {

  def +(v: Vec2): Point = Point(x+v.x, y+v.y)
  def -(v: Vec2): Point = Point(x-v.x, y-v.y)

  def translate(v: Vec2): Unit = {
    this.x += v.x
    this.y += v.y
  }
  
  def unary_- : Point = Point(-x, -y)

  def -(p: Point): Vec2 = Vec2(x - p.x, y - p.y)

  /* trying to find a nice way to create a vector from this to that */
  def -->(that: Point): Vec2 = that - this

  def withX(nx: Float): Point = Point(nx, this.y)
  def withY(ny: Float): Point = Point(this.x, ny)

  def toVec2: Vec2 = Vec2(x, y)
}

object Point {
  implicit def tupleToPoint(p: (Float, Float)): Point = new Point(p._1, p._2)
  implicit def intTupleToPoint(p: (Int, Int)): Point = new Point(p._1.toFloat, p._2.toFloat)
}
