package sgl.math

final case class Vec3(x: Float, y: Float, z: Float) {
  def +(that: Vec3): Vec3 = Vec3(x + that.x, y + that.y, z + that.z)
  def -(that: Vec3): Vec3 = Vec3(x - that.x, y - that.y, z - that.z)
  def *(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)
  def length: Float = scala.math.sqrt(x * x + y * y + z * z).toFloat
  def normalized: Vec3 = {
    val len = length
    if(len == 0f) Vec3.Zero else this * (1f / len)
  }
}

object Vec3 {
  val Zero: Vec3 = Vec3(0f, 0f, 0f)
  val One: Vec3 = Vec3(1f, 1f, 1f)
  val UnitX: Vec3 = Vec3(1f, 0f, 0f)
  val UnitY: Vec3 = Vec3(0f, 1f, 0f)
  val UnitZ: Vec3 = Vec3(0f, 0f, 1f)
}
