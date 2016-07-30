package sgl

import geometry.Point

class Camera {
  var x: Int = 0
  var y: Int = 0

  def coordinates: Point = Point(x, y)

  def cameraToWorld(p: Point): Point = Point(p.x + x, p.y + y)
  def worldToCamera(p: Point): Point = Point(p.x - x, p.y - y)

  override def toString: String = s"Camera(x=$x, y=$y)"
}
