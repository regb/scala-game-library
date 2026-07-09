package sgl

/** Minimal low-level application lifecycle.
  *
  * Games choose their required providers through self-types. The platform runner
  * owns timing, input/event polling, provider initialization, presentation, and
  * shutdown, and calls these hooks.
  */
object Application {
  private[sgl] def framePeriodMillis(fps: Int): Long = {
    require(fps > 0, "Target FPS must be positive")
    scala.math.max(1L, (1000.0 / fps.toDouble).toLong)
  }
}

trait Application {
  def create(): Unit = {}

  /** Advances and renders one frame.
    *
    * @param dt elapsed time since the previous frame, in seconds
    */
  def frame(dt: Double): Unit
  def resize(width: Int, height: Int): Unit = {}
  def pause(): Unit = {}
  def resume(): Unit = {}
  def dispose(): Unit = {}
}
