package sgl.engine.gameobject

/** A color as four components between 0 and 1, alpha not premultiplied. */
final case class Color(red: Float, green: Float, blue: Float, alpha: Float = 1f) {
  def withAlpha(alpha: Float): Color = copy(alpha = alpha)
}

object Color {
  val White: Color = Color(1f, 1f, 1f)
  val Black: Color = Color(0f, 0f, 0f)
  val Transparent: Color = Color(0f, 0f, 0f, 0f)
}
