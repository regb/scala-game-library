package sgl

/** Minimal low-level application lifecycle.
  *
  * Games choose their required providers through self-types. The platform runner
  * owns timing, input/event polling, provider initialization, presentation, and
  * shutdown, and calls these hooks.
  */
trait Application {
  def create(): Unit = {}
  def frame(dt: Double): Unit
  def resize(width: Int, height: Int): Unit = {}
  def pause(): Unit = {}
  def resume(): Unit = {}
  def dispose(): Unit = {}
}
