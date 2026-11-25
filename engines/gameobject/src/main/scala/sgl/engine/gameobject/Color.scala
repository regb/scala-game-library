package sgl.engine.gameobject

/** A color as four components between 0 and 1, alpha not premultiplied. */
final case class Color(red: Float, green: Float, blue: Float, alpha: Float = 1f) {
  def withAlpha(alpha: Float): Color = copy(alpha = alpha)

  /** Each component multiplied: white times a color is that color. */
  def *(other: Color): Color =
    Color(red * other.red, green * other.green, blue * other.blue, alpha * other.alpha)
}

object Color {
  val White: Color = Color(1f, 1f, 1f)
  val Black: Color = Color(0f, 0f, 0f)
  val Transparent: Color = Color(0f, 0f, 0f, 0f)
}
