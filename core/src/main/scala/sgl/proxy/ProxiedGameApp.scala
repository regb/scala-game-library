package sgl
package proxy

trait ProxiedGameApp extends GameApp {

  def startup(): Unit = lifecycleListener.startup()
  def resume(): Unit = lifecycleListener.resume()
  def pause(): Unit = lifecycleListener.pause()
  def shutdown(): Unit = lifecycleListener.shutdown()

  def hasTargetFramePeriodMillis: Boolean = TargetFps.isDefined
  def targetFramePeriodMillis: Long = TargetFps.map(framePeriod).getOrElse(0L)

  def update(dt: Long, canvas: CanvasProxy): Unit

}
