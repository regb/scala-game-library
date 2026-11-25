package sgl.geometry

final case class Vec3(x: Float, y: Float, z: Float) {
  def +(that: Vec3): Vec3 = Vec3(x + that.x, y + that.y, z + that.z)
  def -(that: Vec3): Vec3 = Vec3(x - that.x, y - that.y, z - that.z)
  def *(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)
  def length: Float = scala.math.sqrt(x * x + y * y + z * z).toFloat
  def normalized: Vec3 = {
    val len = length
    if(len == 0f) Vec3.Zero else this * (1f / len)
  }

  /** Linear interpolation: this when `t` is 0, `to` when it is 1. */
  def lerp(to: Vec3, t: Float): Vec3 = this * (1f - t) + to * t

  /** Moves towards `target` by at most `maxStep`, without passing it. */
  def moveTowards(target: Vec3, maxStep: Float): Vec3 = {
    val delta = target - this
    val distance = delta.length
    if(distance <= maxStep) target else this + delta * (maxStep / distance)
  }
}

object Vec3 {
  val Zero: Vec3 = Vec3(0f, 0f, 0f)
  val One: Vec3 = Vec3(1f, 1f, 1f)
  val UnitX: Vec3 = Vec3(1f, 0f, 0f)
  val UnitY: Vec3 = Vec3(0f, 1f, 0f)
  val UnitZ: Vec3 = Vec3(0f, 0f, 1f)
}
