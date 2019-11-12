package sgl.geometry

//TODO: we should differential AABB and OBB
case class Rect(left: Float, top: Float, width: Float, height: Float) {
  require(width >= 0 && height >= 0)

  def right: Float = left + width
  def bottom: Float = top + height

  def centerX = left + width/2
  def centerY = top + height/2
  def center: Point = Point(centerX, centerY)

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
}


object Rect {

  def fromCoordinates(left: Float, top: Float, right: Float, bottom: Float): Rect =
    Rect(left, top, right - left, bottom - top)

}
