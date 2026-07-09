package sgl.math

final case class Mat4(values: Array[Float]) {
  def *(that: Mat4): Mat4 = Mat4.multiply(this, that)
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
