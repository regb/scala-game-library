package sgl.math

final case class Quaternion(x: Float, y: Float, z: Float, w: Float) {
  def *(that: Quaternion): Quaternion = Quaternion(
    w * that.x + x * that.w + y * that.z - z * that.y,
    w * that.y - x * that.z + y * that.w + z * that.x,
    w * that.z + x * that.y - y * that.x + z * that.w,
    w * that.w - x * that.x - y * that.y - z * that.z,
  )
}

object Quaternion {
  val Identity: Quaternion = Quaternion(0f, 0f, 0f, 1f)

  def rotationX(angle: Float): Quaternion = axisAngle(Vec3.UnitX, angle)
  def rotationY(angle: Float): Quaternion = axisAngle(Vec3.UnitY, angle)
  def rotationZ(angle: Float): Quaternion = axisAngle(Vec3.UnitZ, angle)

  def axisAngle(axis: Vec3, angle: Float): Quaternion = {
    val half = angle * 0.5f
    val s = scala.math.sin(half).toFloat
    val n = axis.normalized
    Quaternion(n.x * s, n.y * s, n.z * s, scala.math.cos(half).toFloat)
  }
}
