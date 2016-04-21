package sgl.geometry

import scala.language.implicitConversions

/*
 * Point and Vec are doubles as they are used to simulate physics
 * and using Int (pixels) tend to lose precision when a frame only
 * move modify a fraction of a pixel
 */

case class Point(x: Double, y: Double) {

  def +(m: Vec): Point = Point(x+m.x, y+m.y)
  def -(m: Vec): Point = Point(x-m.x, y-m.y)
  
  def unary_- : Point = Point(-x, -y)

  def -(p: Point): Vec = Vec(x - p.x, y - p.y)

  /* trying to find a nice way to create a vector from this to that */
  def -->(that: Point): Vec = that - this

}

object Point {
  implicit def tupleToPoint(p: (Double, Double)): Point = Point(p._1, p._2)
  implicit def intTupleToPoint(p: (Int, Int)): Point = Point(p._1, p._2)
}

/*
 * We use different class for Point and Vec, even though
 * they can be seen as the same object, because it seems
 * like we don't want to accidently replace one by another
 */
case class Vec(x: Double, y: Double) {

  def +(m: Vec): Vec = Vec(x+m.x, y+m.y)
  def -(m: Vec): Vec = Vec(x-m.x, y-m.y)

  def *(s: Double): Vec = Vec(x*s, y*s)
  
  def unary_- : Vec = Vec(-x, -y)

  def norm: Double = math.sqrt(x*x + y*y)

  def normalized: Vec = {
    val n = this.norm
    Vec(x/n, y/n)
  }

  def isZero: Boolean = x == 0 && y == 0
  def nonZero: Boolean = !isZero

  def pmax(that: Vec): Vec = Vec(x max that.x, y max that.y)
  def pmin(that: Vec): Vec = Vec(x min that.x, y min that.y)

  def dotProduct(that: Vec): Double = this.x*that.x + this.y*that.y
  def *(that: Vec): Double = this.dotProduct(that)

}
