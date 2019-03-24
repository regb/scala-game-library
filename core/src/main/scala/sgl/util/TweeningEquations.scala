package sgl.util

// Not too sure how to export these functionalities, but for
// a start we will just have a static object with one function
// per tweening style (linear, quadratic, or any other crazy shapes).

/** Utils for standard tweening/easing effects.
  *
  * Tweening/easing is used as a way to interpolate intermediate state between
  * a starting and a final state. Typically, it can be used to interpolate a
  * movement from X to Y, by choosing how quickly and with what kind of effect
  * the object should be moved. The simplest kind of tweening is a linear
  * tweening, which will make the object move at constant speed. Because linear
  * tweening is moving at constant speed, it is technically not a form of
  * easing, which should accelerate/decelerate in order to smooth out the
  * velocity from a starting point to an end point (in most cases, from
  * velocity 0 to velocity 0 again, in which case linear tweening wouldn't look
  * very natural).
  *
  * These utils are abstracting on the mathematical formulas, providing a
  * consistent immutable interface (a simple function) with the wanted
  * duration, a start and end state, and the current elapsed time, and will
  * return the point between start and end for the given elapsed time. Using
  * formulas that can give the right state for any current time is a much
  * superior alternative to trying to keep the current position in the game
  * state from frame to frame, as it will naturally correct any rounding issues
  * over time (by recomputing) and ensure that the start and end points will be
  * exact. Besides, some types of easing are mathematically tricky so it makes
  * sense to rely on well-known formulas.
  *
  * Besides linear, the other tweening effects are easing, which will move at
  * different velocity depending on the time in the animation. The standard
  * terminology is to use ease in and ease out, with ease in meaning that the
  * animation starts slow and accelerate, and ease out meaning that the animation
  * starts fast and slows down towards the end. So, for example, easeInQuad is
  * a quadratic tweening, which will start slowly and accelerate, meaning that
  * if we go from 0 to 1, at half of the animation it would be at 0.25 only and
  * cover the remaining 0.75 in the second half of the animation. Sometimes,
  * both are combined, so easeInOut, means that the animation would starts slow,
  * accelerate towards the middle, and then decelerate again from the middle
  * until the end.
  **/
object TweeningEquations {

  // General signature of a tweening function: (duration, start, end) => (duration) => position.
  // The returned position is between start and end, depending on how much elapsed time
  // until the total duration. Abstracting the sort of tweening would be useful for writing
  // the main engine code and tweaking the exact tweening functions.
  // The reasoning for such signature is that we will typically choose parameters for the
  // tweening function (total duration, start and end position) and then query with the
  // same parameters several times as the animation progresses (with different elapsed time).
  // One note about this interface, in the end, it can all be reduced to a [0,1] interval,
  // with the idea that the time/duration is the input between [0,1], the output is
  // the tweening effect between [0,1], which is used to apply to the start/end. For example,
  // a quad tweening could be a function from [0,1] to [0,1], with equation x => x*x, so
  // for 0.5 (half) it returns 0.25. Then the result X can be used in a standard interpolation
  // (1-X)*A + X*B (or A + X(B-A) if that's easier to read) to get from A to B. With that
  // observation, another interface would be really just a function (Double) => Double.
  type TweeningFunction = (Int, Double, Double) => (Int) => Double

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
  
  def linear(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    require(duration > 0)
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else start + (elapsed/duration.toDouble)*(end-start)
  }

  def easeInQuad(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else {
      val progress = elapsed/duration.toDouble
      start + (progress*progress)*(end-start)
    }
  }
  def easeOutQuad(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else {
      val inverseProgress = (duration-elapsed)/duration.toDouble  // 1 -> 0.5 -> 0
      val normalizedQuad = 1 - inverseProgress*inverseProgress  // 0 -> 0.75 -> 1
      start + normalizedQuad*(end-start)
    }
  }
  def easeInOutQuad(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else {
      val progress = elapsed/(duration*0.5)
      if(progress < 1) {
        start + progress*progress*0.5*(end-start)
      } else { // progress is 1 -> 1.5 -> 2
        val newProgress = progress - 1  // 0 -> 0.5 -> 1
        val inverseProgress = 1 - newProgress  // 1 -> 0.5 -> 0
        val normalizedQuad = 1 - inverseProgress*inverseProgress  // 0 -> 0.75 -> 1
        (start + 0.5*(end-start)) + normalizedQuad*0.5*(end-start)
      }
    }
  }
  // TODO: easeOutInQuad

  def easeInCube(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else {
      val progress = elapsed/duration.toDouble
      start + (progress*progress*progress)*(end-start)
    }
  }
  def easeOutCube(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
    if(elapsed <= 0) start
    else if(elapsed >= duration) end
    else {
      val inverseProgress = (duration-elapsed)/duration.toDouble  // 1 -> 0.5 -> 0
      val normalizedCube = 1 - inverseProgress*inverseProgress*inverseProgress  // 0 -> 0.875 -> 1
      start + normalizedCube*(end-start)
    }
  }
  // TODO: easeInOutCube and easeOutInCube

  // Here's the idea of implementating the insight around how all functions can
  // be reduced to a normalized [0,1] operation.
  private def easeGeneric(easeNormalized: (Double) => Double): TweeningFunction = {
    def f(duration: Int, start: Double, end: Double)(elapsed: Int): Double = {
      if(elapsed <= 0) start
      else if(elapsed >= duration) end
      else {
        val x = easeNormalized(elapsed/duration.toDouble)
        start + x*(end-start)
      }
    }
    f
  }

  /** Compute the normalized easeOutSine.
    *
    * The sine-based ease-out uses the sine function curve between 0 and PI/2,
    * which is a very light easing (almost linear, even less strong than
    * quadratic). It is an out easing because the sine function between 0 and
    * PI/2 goes up slightly faster first before flattening.
    */
  private def easeOutSineNormalized(t: Double): Double = {
    math.sin(t*math.Pi/2)
  }
  def easeOutSine: TweeningFunction = easeGeneric(easeOutSineNormalized)
  /** Compute the normalized easeInSine.
    *
    * Just as the easeOutSine, this is based on the sine function curve, but
    * here it is the portion between [3PI/2, 2PI], which has an ease-in shape
    * from -1 to 0, and is then shifted by +1 to bring back to the normalized
    * area.
    */
  private def easeInSineNormalized(t: Double): Double = {
    math.sin(t*math.Pi/2 + 1.5*math.Pi) + 1
  }
  def easeInSine: TweeningFunction = easeGeneric(easeInSineNormalized)
  //TODO: easeInOutSine and easeOutInSine


  /** Compute the normalized easeInExp.
    *
    * This is based on an exponential function, hence is a much stronger
    * acceleration and deceleration than any of the polynomial-based functions.
    * The idea is still to go from 0 to 1. An exponential function
    * traditionnaly tend to 0 when put at -infinity, pass by 1 at point 0, (
    * pass by the base at point 1), and tend to +infinity as the input goes to
    * +infinity. The idea is to bring back this function into the [0,1]
    * interval.
    *
    * For ease-in, the shape of the exponential good from -infinity to 0, and
    * it goes from 0 to 1, which are output values that we want. So we just
    * need to map the input 0 to -infinity and 1 to 0 and then apply the
    * standard exponential function on the mapping. We can take A*(t-1), which
    * grows to -A when t is 0 and is 0 when t is 1. Now it's worth noting that the
    * exponential is quickly going to 0 as the exponent becomes small, in fact even
    * e^(-3) is already extremely close to 0, so A can be anything > 3 and it
    * should be good enough. One standard in the web is to use A=10, which we can
    * suggest as default, but the function can be made general in any case. The
    * base of the exponent can be anything, 2 or e are standard as well.
    * TODO: maybe the mapping should be from -infinity to +infinity for the input, and
    *       then the output is from 0 to +infinity but we just want to map it to 0-1, this
    *       might give a better true exponential shape.
    */
  private def easeInExpNormalized(b: Double = 2, a: Double = 10)(t: Double): Double = {
    math.pow(b, a*(t-1))
  }
  def easeInExp: TweeningFunction = easeGeneric(easeInExpNormalized())
  /** Compute the normalized easeInExp.
    *
    * Most of the reasoning behind the easeInExp applies here. But we just want
    * to flip the curve, so still from -infinity to 0, but we want to flip the x/y
    * axis. This could be accomplished by bringing a log, but it can also be done
    * by tweaking the equation a bit. Essentially, we can make the equation going
    * from 1 to 0 instead of 0 to 1, by using -A*t, it would keep the shape, that
    * is it quickly goes towards 0 and then flattens. Taking 1 - this will tend
    * get the 0 to 1 with the out shape.
    */
  private def easeOutExpNormalized(b: Double = 2, a: Double = 10)(t: Double): Double = {
    1 - math.pow(b, -a*t)
  }
  def easeOutExp: TweeningFunction = easeGeneric(easeOutExpNormalized())
  //TODO: easeInOutExp and easeOutInExp

  // TODO: We need a bounce easing, where the state goes to end, then back up a bit, then
  //       again to end, then back less, etc until stopping at end (like a ball bouncing on
  //       the floor. This would never go over end, and that is in contrast to the following
  //       easing that we want: an elastic based on sine, and an easing that overshoot and
  //       slowy go back to end coming from above (elastic oscillates many times, the other one
  //       just go once over and come back to halt).

  /** Compute an elastic easing effect by using a sine function.
    *
    * The idea is to start at 0 and end at 1, while oscillating slightly over 1
    * through the course of the move. The oscilliation gets dampened more and
    * more, which is achieved through an amplifier that becomes smaller and
    * smaller as we reach toward the end of the animation.
    *
    * As it is an ease-out, it starts strong and quickly overshoots 1, then
    * slows down as it osciliates toward the end state of 1. Parameters that
    * can be controled are the strength of the amplification (a) and the
    * frequency of the period (p). Let us try to derive the mathematical
    * formula to accomplish this effect.
    *
    * Let t be [0,1], then cos(t * (2*Pi)) will have exactly one cycle,
    * starting at 1, and ending at 1 with a full period (going all the way down
    * to -1).  Let's introduce the repetition (period) p, now cos(t * p*(2*Pi))
    * is going to be how many times we cycle entirely. So if p=2, we will
    * double the number of cycles, so a more intense oscilliation.
    *
    * At this point, we have a simple function that osciliate and we can choose
    * how often it does it. But we need to combine it somehow to generate an
    * easing effect that starts from 0, end at 1, and is going from strong to
    * weak. Going from strong to weak can be controlled by changing the
    * amplitude of the signal, and this change can be done using something
    * similar to the exponential easing function. The final insight, to go from
    * 0 to 1, is that the amplitude will naturally start strong and end close
    * to 0, so at the end since the amplitude is 0, we will need the function
    * to return the final point, or 1 +/- A*cos. Since the amplitude is strong
    * when we start (in practice, that is 1), and the cos starts at 1, we want
    * to do 1 - A*cos to start at 0 and end at 1.
    *
    * The amplitude should be computed to start at 1 and go to 0 in an
    * exponential way, which can be done by math.pow(2, -A*t). A can be set to
    * control how quickly we want to get to low amplitude, but 10 seems to be a
    * good default.  Note that A can be set anywhere larger than 0, but the
    * smallest values will mean that the amplitude will be very strong for a
    * very long time, and essentially it will make it look like the value at 1
    * might still be quite far from 1 because the amplitude is still large. In
    * practice 7 seems to be the minimum below which the end of the animation
    * can look bad (discrete jump to bridge the final state). Note that since
    * the amplitude is always between [0,1], the ease effect will go very fast
    * slightly above 1 (with a theoretical max of 2 due to the cos, but with
    * realistic value it's more like 1.4) and then slightly below again.
    *
    * Let's put everything together, the function is 1 - math.pow(2,
    * -A*t)*math.cos(t*P*(2*math.Pi)) with A (default 10) and P (default 2)
    * that can be played with to get stronger amplitude and more cycles.
    */
  private def easeOutElasticNormalized(a: Double = 10, p: Double = 2)(t: Double): Double =
    1 - math.pow(2, -a*t)*math.cos(t*p*(2*math.Pi))
  def easeOutElastic(a: Double = 10, p: Double = 2): TweeningFunction = easeGeneric(easeOutElasticNormalized(a, p))

}
