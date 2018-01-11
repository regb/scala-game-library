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
  
  def linear(elapsed: Int, start: Double, end: Double, duration: Int): Double = {
    start + (elapsed/duration.toDouble)*(end-start)
  }

}
