package sgl.util

object Math {

  val Pi = scala.math.Pi

  def degreeToRadian(degree: Double): Double = (degree/180d)*Pi
  def radianToDegree(radian: Double): Double = (radian*180d)/Pi

}
