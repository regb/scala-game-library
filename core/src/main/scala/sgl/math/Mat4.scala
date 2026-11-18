package sgl.math

final case class Mat4(values: Array[Float]) {
  def *(that: Mat4): Mat4 = Mat4.multiply(this, that)

  /** The matrix undoing this one. A camera's view matrix is the inverse of
    * the camera's own transform.
    */
  def inverse: Mat4 = Mat4.invert(this)
}

object Mat4 {
  val Identity: Mat4 = Mat4(Array[Float](
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f,
  ))

  def scale(x: Float, y: Float, z: Float): Mat4 = Mat4(Array[Float](
    x, 0f, 0f, 0f,
    0f, y, 0f, 0f,
    0f, 0f, z, 0f,
    0f, 0f, 0f, 1f,
  ))

  def translation(x: Float, y: Float, z: Float): Mat4 = Mat4(Array[Float](
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    x,  y,  z,  1f,
  ))

  def translation(v: Vec3): Mat4 = translation(v.x, v.y, v.z)

  def rotation(q: Quaternion): Mat4 = {
    val xx = q.x * q.x
    val yy = q.y * q.y
    val zz = q.z * q.z
    val xy = q.x * q.y
    val xz = q.x * q.z
    val yz = q.y * q.z
    val wx = q.w * q.x
    val wy = q.w * q.y
    val wz = q.w * q.z

    Mat4(Array[Float](
      1f - 2f * (yy + zz), 2f * (xy + wz),      2f * (xz - wy),      0f,
      2f * (xy - wz),      1f - 2f * (xx + zz), 2f * (yz + wx),      0f,
      2f * (xz + wy),      2f * (yz - wx),      1f - 2f * (xx + yy), 0f,
      0f,                  0f,                  0f,                  1f,
    ))
  }

  def rotationX(angle: Float): Mat4 = rotation(Quaternion.rotationX(angle))
  def rotationY(angle: Float): Mat4 = rotation(Quaternion.rotationY(angle))

  def trs(position: Vec3, rotation: Quaternion, scale: Vec3): Mat4 =
    translation(position) * Mat4.rotation(rotation) * Mat4.scale(scale.x, scale.y, scale.z)

  def perspective(fovyRadians: Float, aspect: Float, near: Float, far: Float): Mat4 = {
    val f = (1.0 / scala.math.tan(fovyRadians / 2.0)).toFloat
    val nf = 1f / (near - far)
    Mat4(Array[Float](
      f / aspect, 0f, 0f, 0f,
      0f, f, 0f, 0f,
      0f, 0f, (far + near) * nf, -1f,
      0f, 0f, 2f * far * near * nf, 0f,
    ))
  }

  def lookAt(eye: Vec3, target: Vec3, up: Vec3): Mat4 = {
    val forward = (target - eye).normalized
    val side = cross(forward, up).normalized
    val cameraUp = cross(side, forward)
    Mat4(Array[Float](
      side.x, cameraUp.x, -forward.x, 0f,
      side.y, cameraUp.y, -forward.y, 0f,
      side.z, cameraUp.z, -forward.z, 0f,
      -dot(side, eye), -dot(cameraUp, eye), dot(forward, eye), 1f,
    ))
  }

  def invert(matrix: Mat4): Mat4 = {
    val m = matrix.values
    val inv = new Array[Float](16)

    inv(0) = m(5) * m(10) * m(15) - m(5) * m(11) * m(14) - m(9) * m(6) * m(15) + m(9) * m(7) * m(14) + m(13) * m(6) * m(11) - m(13) * m(7) * m(10)
    inv(4) = -m(4) * m(10) * m(15) + m(4) * m(11) * m(14) + m(8) * m(6) * m(15) - m(8) * m(7) * m(14) - m(12) * m(6) * m(11) + m(12) * m(7) * m(10)
    inv(8) = m(4) * m(9) * m(15) - m(4) * m(11) * m(13) - m(8) * m(5) * m(15) + m(8) * m(7) * m(13) + m(12) * m(5) * m(11) - m(12) * m(7) * m(9)
    inv(12) = -m(4) * m(9) * m(14) + m(4) * m(10) * m(13) + m(8) * m(5) * m(14) - m(8) * m(6) * m(13) - m(12) * m(5) * m(10) + m(12) * m(6) * m(9)
    inv(1) = -m(1) * m(10) * m(15) + m(1) * m(11) * m(14) + m(9) * m(2) * m(15) - m(9) * m(3) * m(14) - m(13) * m(2) * m(11) + m(13) * m(3) * m(10)
    inv(5) = m(0) * m(10) * m(15) - m(0) * m(11) * m(14) - m(8) * m(2) * m(15) + m(8) * m(3) * m(14) + m(12) * m(2) * m(11) - m(12) * m(3) * m(10)
    inv(9) = -m(0) * m(9) * m(15) + m(0) * m(11) * m(13) + m(8) * m(1) * m(15) - m(8) * m(3) * m(13) - m(12) * m(1) * m(11) + m(12) * m(3) * m(9)
    inv(13) = m(0) * m(9) * m(14) - m(0) * m(10) * m(13) - m(8) * m(1) * m(14) + m(8) * m(2) * m(13) + m(12) * m(1) * m(10) - m(12) * m(2) * m(9)
    inv(2) = m(1) * m(6) * m(15) - m(1) * m(7) * m(14) - m(5) * m(2) * m(15) + m(5) * m(3) * m(14) + m(13) * m(2) * m(7) - m(13) * m(3) * m(6)
    inv(6) = -m(0) * m(6) * m(15) + m(0) * m(7) * m(14) + m(4) * m(2) * m(15) - m(4) * m(3) * m(14) - m(12) * m(2) * m(7) + m(12) * m(3) * m(6)
    inv(10) = m(0) * m(5) * m(15) - m(0) * m(7) * m(13) - m(4) * m(1) * m(15) + m(4) * m(3) * m(13) + m(12) * m(1) * m(7) - m(12) * m(3) * m(5)
    inv(14) = -m(0) * m(5) * m(14) + m(0) * m(6) * m(13) + m(4) * m(1) * m(14) - m(4) * m(2) * m(13) - m(12) * m(1) * m(6) + m(12) * m(2) * m(5)
    inv(3) = -m(1) * m(6) * m(11) + m(1) * m(7) * m(10) + m(5) * m(2) * m(11) - m(5) * m(3) * m(10) - m(9) * m(2) * m(7) + m(9) * m(3) * m(6)
    inv(7) = m(0) * m(6) * m(11) - m(0) * m(7) * m(10) - m(4) * m(2) * m(11) + m(4) * m(3) * m(10) + m(8) * m(2) * m(7) - m(8) * m(3) * m(6)
    inv(11) = -m(0) * m(5) * m(11) + m(0) * m(7) * m(9) + m(4) * m(1) * m(11) - m(4) * m(3) * m(9) - m(8) * m(1) * m(7) + m(8) * m(3) * m(5)
    inv(15) = m(0) * m(5) * m(10) - m(0) * m(6) * m(9) - m(4) * m(1) * m(10) + m(4) * m(2) * m(9) + m(8) * m(1) * m(6) - m(8) * m(2) * m(5)

    val determinant = m(0) * inv(0) + m(1) * inv(4) + m(2) * inv(8) + m(3) * inv(12)
    if(determinant == 0f) throw new ArithmeticException("Matrix is not invertible")
    val scale = 1f / determinant
    var i = 0
    while(i < 16) {
      inv(i) *= scale
      i += 1
    }
    Mat4(inv)
  }

  def dot(a: Vec3, b: Vec3): Float = a.x * b.x + a.y * b.y + a.z * b.z

  def cross(a: Vec3, b: Vec3): Vec3 = Vec3(
    a.y * b.z - a.z * b.y,
    a.z * b.x - a.x * b.z,
    a.x * b.y - a.y * b.x,
  )

  def multiply(a: Mat4, b: Mat4): Mat4 = {
    val av = a.values
    val bv = b.values
    val out = new Array[Float](16)
    var col = 0
    while(col < 4) {
      var row = 0
      while(row < 4) {
        out(col * 4 + row) =
          av(row) * bv(col * 4) +
          av(4 + row) * bv(col * 4 + 1) +
          av(8 + row) * bv(col * 4 + 2) +
          av(12 + row) * bv(col * 4 + 3)
        row += 1
      }
      col += 1
    }
    Mat4(out)
  }
}
