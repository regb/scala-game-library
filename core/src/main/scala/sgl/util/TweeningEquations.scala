package sgl.util

// Not too sure how to export these functionalities, but for
// a start we will just have a static object with one function
// per tweening style (linear, quadratic, or any other crazy shapes).
object TweeningEquations {

  // General signature of a tweening function: (elapsed, start, end, duration) => position.
  // The returned position is between start and end, depending on how much elapsed time
  // until the total duration. Abstracting the sort of tweening would be useful for writing
  // the main engine code and tweaking the exact tweening functions.
  type TweeningFunction = (Int, Double, Double, Int) => Double

  /*
   * One design consideration is what to do when the elapsed time is out of bound
   * (negative or larger than duration). There are essentially three reasonable
   * options:
   *
   *   1) Clamp to the start and end values. That makes sense if we assume that
   *      the tweening equations are used within some kind of physic simulation
   *      (as it is the case with games) and that there are natural imprecisions that
   *      could lead to the time going slightly over the total duration.
   *   2) As in 1, accept values out of bound but also compute an out-of-bound value.
   *      For example, for a tweening of 100 seconds, from 0 to 1, if the elapsed
   *      time reached 110, then just return the value 1.1.
   *   3) Enforce elapsed time to be within the right bounds, and crash (with a
   *      an exception) if not.
   *
   * All three have some appeal. Number 3 is kind of the safest which can ensure
   * we catch errors as quickly as possible. Number 2 is sort of the smoothest
   * because it will overflow the result in the same way the input overflow. It is
   * also the cheapest as the implementation will remain a direct formula without
   * edge testing. Number 1 is interesting because it will eliminate a small imprecision
   * in the inputs by returning "safe" output (within the expected bounds).
   *
   * The current design decision is to go with option 1, because I believe this
   * is the most common case. The tweening will often be used to derive a scale
   * or transparency factor from 0f to 1f, and it does not make any sense to overflow
   * the output. On the other hand, it is easy to have the elapsed time overflowing
   * slightly the total duration, simply because each simulation frame adds a discrete
   * amount of ms to the elapsed time, and the last frame could easily overflow.
   *
   * In theory, option 2 is the most flexible, because the caller could add their
   * own bound checking afterwards. But despite that, it seems that the most common
   * would be to ensure option 1, so we could as well choose that design internally.
   *
   * However, we enforce duration to be strictly positive (no reason for it to ever
   * be 0 or negative) and check it with preconditions. start and end should essentially
   * be allowed to be anything, as it makes just as much sense to tween a decreasing value
   * than an increasing one.
   */
  
  def linear(elapsed: Int, start: Double, end: Double, duration: Int): Double = {
    require(duration > 0)
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else start + (elapsed/duration.toDouble)*(end-start)
  }

}
