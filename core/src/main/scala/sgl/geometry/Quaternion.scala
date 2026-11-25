package sgl.geometry

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

  /** The orientation of something facing along `forward` with `up` roughly
    * overhead, in the convention cameras use: its local -Z axis points along
    * `forward`, +Y towards `up` and +X to the right. `forward` and `up` must
    * not be parallel.
    */
  def lookAlong(forward: Vec3, up: Vec3): Quaternion = {
    val f = forward.normalized
    val side = Mat4.cross(f, up).normalized
    val u = Mat4.cross(side, f)

    // Rotation matrix with columns (side, u, -f), converted to a quaternion.
    val m00 = side.x; val m10 = side.y; val m20 = side.z
    val m01 = u.x; val m11 = u.y; val m21 = u.z
    val m02 = -f.x; val m12 = -f.y; val m22 = -f.z
    val trace = m00 + m11 + m22
    if(trace > 0f) {
      val s = scala.math.sqrt(trace + 1.0).toFloat * 2f
      Quaternion((m21 - m12) / s, (m02 - m20) / s, (m10 - m01) / s, 0.25f * s)
    } else if(m00 > m11 && m00 > m22) {
      val s = scala.math.sqrt(1.0 + m00 - m11 - m22).toFloat * 2f
      Quaternion(0.25f * s, (m01 + m10) / s, (m02 + m20) / s, (m21 - m12) / s)
    } else if(m11 > m22) {
      val s = scala.math.sqrt(1.0 + m11 - m00 - m22).toFloat * 2f
      Quaternion((m01 + m10) / s, 0.25f * s, (m12 + m21) / s, (m02 - m20) / s)
    } else {
      val s = scala.math.sqrt(1.0 + m22 - m00 - m11).toFloat * 2f
      Quaternion((m02 + m20) / s, (m12 + m21) / s, 0.25f * s, (m10 - m01) / s)
    }
  }

  def axisAngle(axis: Vec3, angle: Float): Quaternion = {
    val half = angle * 0.5f
    val s = scala.math.sin(half).toFloat
    val n = axis.normalized
    Quaternion(n.x * s, n.y * s, n.z * s, scala.math.cos(half).toFloat)
  }
}
