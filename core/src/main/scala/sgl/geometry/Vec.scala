package sgl.geometry

//import scala.language.implicitConversions

/*
 * We use different class for Point and Vec, even though
 * they can be seen as the same object, because it seems
 * like we don't want to accidently replace one by another
 */
case class Vec(x: Float, y: Float) {

  def +(m: Vec): Vec = Vec(x+m.x, y+m.y)
  def -(m: Vec): Vec = Vec(x-m.x, y-m.y)

  def *(s: Float): Vec = Vec(x*s, y*s)
  
  def unary_- : Vec = Vec(-x, -y)

  def norm: Float = math.sqrt(x*x + y*y).toFloat

  def normalized: Vec = {
    val n = this.norm
    Vec(x/n, y/n)
  }

  def isZero: Boolean = x == 0 && y == 0
  def nonZero: Boolean = !isZero

  def pmax(that: Vec): Vec = Vec(x max that.x, y max that.y)
  def pmin(that: Vec): Vec = Vec(x min that.x, y min that.y)

  def dotProduct(that: Vec): Float = this.x*that.x + this.y*that.y
  def *(that: Vec): Float = this.dotProduct(that)

  //TODO: could define +=, *=, -= as mutating the Vec? would make much
  //      code similar, we would still keep +/*/- as operation that
  //      produces new Vec, but there would be the option of not creating garbage

  //clockwise as the standard mathematical interpetation (with y pointing up),
  //so might be reversed in the game engine world
  def clockwisePerpendicular: Vec = Vec(-y, x)

  def toPoint: Point = Point(x, y)
}
