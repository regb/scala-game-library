package sgl.geometry

/** an AABB Rect.
  *
  * This class is mutable, and several of its methods modify the state instead
  * of just returning a new Rect. This is very much not idiomatic Scala (which
  * favors immutable objects), but this is also a trade-off necessary for games
  * to avoid generating too much garbage to collect. Time will tell if this
  * design decision was good or bad.
  **/
class Rect(var left: Float, var top: Float, var width: Float, var height: Float) {

  def right: Float = left + width
  def bottom: Float = top + height

  def centerX = left + width/2
  def centerY = top + height/2
  def center: Point = Point(centerX, centerY)

  def +(m: Vec): Rect = Rect(left + m.x, top + m.y, width, height)
  def -(m: Vec): Rect = Rect(left - m.x, top - m.y, width, height)

  /*
   * names are inversed with (x,y) coordinates, unfortunate...
   */
  def topLeft: Point = Point(left, top)
  def topRight: Point = Point(right, top)
  def bottomLeft: Point = Point(left, bottom)
  def bottomRight: Point = Point(right, bottom)

  def vertices: Set[Point] = Set(topLeft, topRight, bottomLeft, bottomRight)

  //maybe intersect should go into external objects since there is no notion of direction (point vs rect)
  def intersect(x: Float, y: Float): Boolean =
    x >= left && x <= right && y >= top && y <= bottom

  def intersect(point: Point): Boolean = intersect(point.x, point.y)

  def intersect(rect: Rect): Boolean = Collisions.aabbWithAabb(this, rect)

  def intersect(circle: Circle): Boolean = Collisions.circleWithAabb(circle, this)

  override def toString: String = s"Rect(left=$left, top=$top, width=$width, height=$height)"

  override def clone: Rect = Rect(left, top, width, height)
}

object Rect {

  def apply(left: Float, top: Float, width: Float, height: Float) = new Rect(left, top, width, height)

  def fromBoundingBox(left: Float, top: Float, right: Float, bottom: Float): Rect =
    Rect(left, top, right - left, bottom - top)

}
