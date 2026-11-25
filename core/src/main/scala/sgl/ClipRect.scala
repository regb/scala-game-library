package sgl

/** Where drawing is limited to, in pixels from the top left of the window. */
private[sgl] final case class ClipRect(left: Float, top: Float, right: Float, bottom: Float) {
  def intersect(other: ClipRect): ClipRect = ClipRect(
    scala.math.max(left, other.left),
    scala.math.max(top, other.top),
    scala.math.min(right, other.right),
    scala.math.min(bottom, other.bottom)
  )
}

private[sgl] object ClipRect {
  /** Limits what is drawn from now on, or nothing with `None`. What is batched
    * has to be drawn before.
    */
  def set(gl: OpenGLProvider#OpenGL, clip: Option[ClipRect], windowWidth: Int, windowHeight: Int): Unit = clip match {
    case Some(value) =>
      val left = scala.math.max(0, scala.math.floor(value.left).toInt)
      val top = scala.math.max(0, scala.math.floor(value.top).toInt)
      val right = scala.math.min(windowWidth, scala.math.ceil(value.right).toInt)
      val bottom = scala.math.min(windowHeight, scala.math.ceil(value.bottom).toInt)
      gl.enable(gl.ScissorTest)
      // OpenGL counts from the bottom.
      gl.scissor(left, windowHeight - bottom, scala.math.max(0, right - left), scala.math.max(0, bottom - top))
    case None => gl.disable(gl.ScissorTest)
  }
}
