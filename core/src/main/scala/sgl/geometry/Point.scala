package sgl.geometry

import scala.language.implicitConversions

/*
 * Point and Vec are floats as they are used to simulate physics
 * and using Int (pixels) tend to lose precision when a frame only
 * move a fraction of a pixel.
 */

case class Point(x: Float, y: Float) {

  def +(m: Vec): Point = Point(x+m.x, y+m.y)
  def -(m: Vec): Point = Point(x-m.x, y-m.y)
  
  def unary_- : Point = Point(-x, -y)

  def -(p: Point): Vec = Vec(x - p.x, y - p.y)

  /* trying to find a nice way to create a vector from this to that */
  def -->(that: Point): Vec = that - this

  def toVec: Vec = Vec(x, y)
}

object Point {
  implicit def tupleToPoint(p: (Float, Float)): Point = Point(p._1, p._2)
  implicit def intTupleToPoint(p: (Int, Int)): Point = Point(p._1, p._2)
}
