package sgl.geometry

case class Circle(x: Int, y: Int, radius: Int) {
  require(radius >= 0)

  def center: Point = Point(x, y)

  def left: Int = x - radius
  def top: Int = y - radius
  def right: Int = x + radius
  def bottom: Int = y + radius


  /* contrary to Rec, the intersect takes double as part of the sphere is in-between
   * two pixels
   */
  def intersect(x: Double, y: Double): Boolean = {
    val d2 = (x - this.x)*(x - this.x) + (y - this.y)*(y - this.y)
    d2 <= radius*radius
  }

  def intersect(point: Point): Boolean = intersect(point.x, point.y)

  def intersect(that: Circle): Boolean = Collisions.circleWithCircle(this, that)

  def intersect(rect: Rect): Boolean = Collisions.circleWithAabb(this, rect)

  def boundingRect: Rect = Rect(x - radius, y - radius, 2*radius, 2*radius)
}
