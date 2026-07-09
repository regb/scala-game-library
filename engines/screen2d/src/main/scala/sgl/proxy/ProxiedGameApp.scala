package sgl
package proxy

trait ProxiedGameApp extends Screen2DGameApp {

  def startup(): Unit = create()
  override def resume(): Unit = super.resume()
  override def pause(): Unit = super.pause()
  def shutdown(): Unit = dispose()

  def hasTargetFramePeriodMillis: Boolean = TargetFps.isDefined
  def targetFramePeriodMillis: Long = TargetFps.map(framePeriod).getOrElse(0L)

  def update(dt: Long, canvas: CanvasProxy): Unit

}
