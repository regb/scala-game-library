package sgl.geometry

case class Circle(x: Float, y: Float, radius: Float) {
  require(radius >= 0)

  def center: Point = Point(x, y)

  def left: Float = x - radius
  def top: Float = y - radius
  def right: Float = x + radius
  def bottom: Float = y + radius

  def +(m: Vec): Circle = Circle(x + m.x, y + m.y, radius)
  def -(m: Vec): Circle = Circle(x - m.x, y - m.y, radius)

  def intersect(x: Float, y: Float): Boolean = {
    val d2 = (x - this.x)*(x - this.x) + (y - this.y)*(y - this.y)
    d2 <= radius*radius
  }

  def intersect(point: Point): Boolean = intersect(point.x, point.y)

  def intersect(that: Circle): Boolean = Collisions.circleWithCircle(this, that)

  def intersect(rect: Rect): Boolean = Collisions.circleWithAabb(this, rect)

  def boundingBox: Rect = Rect(x - radius, y - radius, 2*radius, 2*radius)

  def asEllipse: Ellipse = Ellipse(x, y, 2*radius, 2*radius)
}

object Circle {
  def apply(center: Point, radius: Float): Circle = Circle(center.x, center.y, radius)
}


case class Ellipse(x: Float, y: Float, width: Float, height: Float) {

  def center: Point = Point(x, y)

  def left: Float = x - width/2
  def top: Float = y - height/2
  def right: Float = x + width/2
  def bottom: Float = y + height/2

  def +(m: Vec): Ellipse = Ellipse(x + m.x, y + m.y, width, height)
  def -(m: Vec): Ellipse = Ellipse(x - m.x, y - m.y, width, height)

  def boundingBox: Rect = Rect(x - width/2, y - height/2, width, height)
}
