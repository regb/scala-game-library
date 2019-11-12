package sgl.geometry

case class Circle(x: Float, y: Float, radius: Float) {
  require(radius >= 0)

  def center: Point = Point(x, y)

  def left: Float = x - radius
  def top: Float = y - radius
  def right: Float = x + radius
  def bottom: Float = y + radius


  /* contrary to Rec, the intersect takes double as part of the sphere is in-between
   * two pixels
   */
  def intersect(x: Float, y: Float): Boolean = {
    val d2 = (x - this.x)*(x - this.x) + (y - this.y)*(y - this.y)
    d2 <= radius*radius
  }

  def intersect(point: Point): Boolean = intersect(point.x, point.y)

  def intersect(that: Circle): Boolean = Collisions.circleWithCircle(this, that)

  def intersect(rect: Rect): Boolean = Collisions.circleWithAabb(this, rect)

  def boundingRect: Rect = Rect(x - radius, y - radius, 2*radius, 2*radius)
}

object Circle {
  def apply(center: Point, radius: Float): Circle = Circle(center.x, center.y, radius)
}
