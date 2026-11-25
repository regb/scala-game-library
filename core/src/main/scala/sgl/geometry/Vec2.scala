package sgl.geometry

//import scala.language.implicitConversions

/*
 * We use different class for Point and Vec2, even though
 * they can be seen as the same object, because it seems
 * like we don't want to accidently replace one by another.
 */
case class Vec2(x: Float, y: Float) {

  def +(m: Vec2): Vec2 = Vec2(x+m.x, y+m.y)
  def -(m: Vec2): Vec2 = Vec2(x-m.x, y-m.y)

  def *(s: Float): Vec2 = Vec2(x*s, y*s)
  
  def unary_- : Vec2 = Vec2(-x, -y)

  def norm: Float = scala.math.sqrt(x*x + y*y).toFloat

  def normal: Vec2 = Vec2(-y, x)

  def normalized: Vec2 = {
    val n = this.norm
    Vec2(x/n, y/n)
  }

  /** Linear interpolation: this when `t` is 0, `to` when it is 1. */
  def lerp(to: Vec2, t: Float): Vec2 = this * (1f - t) + to * t

  /** Moves towards `target` by at most `maxStep`, without passing it. */
  def moveTowards(target: Vec2, maxStep: Float): Vec2 = {
    val delta = target - this
    val distance = delta.norm
    if(distance <= maxStep) target else this + delta * (maxStep / distance)
  }

  def isZero: Boolean = x == 0 && y == 0
  def nonZero: Boolean = !isZero

  def pmax(that: Vec2): Vec2 = Vec2(x max that.x, y max that.y)
  def pmin(that: Vec2): Vec2 = Vec2(x min that.x, y min that.y)

  def dotProduct(that: Vec2): Float = this.x*that.x + this.y*that.y
  def *(that: Vec2): Float = this.dotProduct(that)

  //TODO: could define +=, *=, -= as mutating the Vec2? would make much
  //      code similar, we would still keep +/*/- as operation that
  //      produces new Vec2, but there would be the option of not creating garbage

  //clockwise as the standard mathematical interpetation (with y pointing up),
  //so might be reversed in the game engine world
  def clockwisePerpendicular: Vec2 = Vec2(-y, x)

  def toPoint: Point = Point(x, y)
}
