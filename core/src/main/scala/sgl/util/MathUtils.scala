package sgl.util

/** Numeric helpers that games use often and that scala.math does not have.
  *
  * Angles are in radians. For curves over a fixed duration, see
  * [[TweeningEquations]]; for vectors and rotations, see `lerp` and `slerp` in
  * sgl.geometry.
  */
object MathUtils {

  private val Pi = scala.math.Pi.toFloat

  /** `value`, kept between `min` and `max`. */
  def clamp(value: Float, min: Float, max: Float): Float =
    if(value < min) min else if(value > max) max else value
  def clamp(value: Double, min: Double, max: Double): Double =
    if(value < min) min else if(value > max) max else value
  def clamp(value: Int, min: Int, max: Int): Int =
    if(value < min) min else if(value > max) max else value

  /** Linear interpolation: `from` when `t` is 0, `to` when it is 1.
    *
    * A `t` outside of [0, 1] extrapolates.
    */
  def lerp(from: Float, to: Float, t: Float): Float = from * (1f - t) + to * t
  def lerp(from: Double, to: Double, t: Double): Double = from * (1.0 - t) + to * t

  /** The `t` for which `lerp(from, to, t)` is `value`, or 0 if `from` is `to`. */
  def inverseLerp(from: Float, to: Float, value: Float): Float =
    if(from == to) 0f else (value - from) / (to - from)

  /** Maps `value` from the range [inMin, inMax] to the range [outMin, outMax], without clamping. */
  def remap(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float =
    lerp(outMin, outMax, inverseLerp(inMin, inMax, value))

  /** 0 up to `edge0`, 1 from `edge1`, and a smooth curve in between. */
  def smoothstep(edge0: Float, edge1: Float, value: Float): Float =
    if(edge0 == edge1) { if(value < edge0) 0f else 1f }
    else {
      val t = clamp(inverseLerp(edge0, edge1, value), 0f, 1f)
      t * t * (3f - 2f * t)
    }

  /** Moves `current` towards `target` by at most `maxStep`, without passing it. */
  def moveTowards(current: Float, target: Float, maxStep: Float): Float =
    if(scala.math.abs(target - current) <= maxStep) target
    else if(current < target) current + maxStep
    else current - maxStep

  /** Moves `current` smoothly towards `target`, fast when far and slow when near.
    *
    * Call it every frame with the frame's `dt`. Unlike a lerp with a constant
    * factor, the movement is the same at any frame rate. A higher `rate` is
    * faster: the remaining distance is divided by about 2.7 every `1 / rate`
    * seconds.
    */
  def damp(current: Float, target: Float, rate: Float, dt: Float): Float =
    lerp(target, current, scala.math.exp(-rate * dt).toFloat)

  /** Wraps `value` around into [min, max), like a world that repeats. */
  def wrap(value: Float, min: Float, max: Float): Float = {
    val range = max - min
    val wrapped = value - range * scala.math.floor((value - min) / range).toFloat
    // Rounding can land exactly on, or just outside of, the bounds.
    if(wrapped >= max || wrapped < min) min else wrapped
  }

  /** The same angle, in [-Pi, Pi). */
  def wrapAngle(angle: Float): Float = wrap(angle, -Pi, Pi)

  /** The shortest turn from one angle to another, in [-Pi, Pi). */
  def deltaAngle(from: Float, to: Float): Float = wrap(to - from, -Pi, Pi)

  /** Interpolates between two angles along the shortest turn. */
  def lerpAngle(from: Float, to: Float, t: Float): Float = from + deltaAngle(from, to) * t

  /** Turns `current` towards `target` by at most `maxStep`, along the shortest turn. */
  def moveTowardsAngle(current: Float, target: Float, maxStep: Float): Float = {
    val delta = deltaAngle(current, target)
    if(scala.math.abs(delta) <= maxStep) target
    else current + (if(delta < 0f) -maxStep else maxStep)
  }
}
