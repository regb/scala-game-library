package sgl
package proxy

/** Application lifecycle exposed to proxy-based platform runners. */
trait ProxiedGameApp extends Application {

  /** Proxy runners use 60 FPS unless an application family overrides it. */
  def TargetFps: Option[Int] = Some(60)

  def startup(): Unit = create()
  override def resume(): Unit = super.resume()
  override def pause(): Unit = super.pause()
  def shutdown(): Unit = dispose()

  def hasTargetFramePeriodMillis: Boolean = TargetFps.isDefined
  def targetFramePeriodMillis: Long = TargetFps.map(Application.framePeriodMillis).getOrElse(0L)

  /** Advances one frame using the platform canvas. `dt` is in milliseconds at
    * this Java/Kotlin compatibility boundary.
    */
  def update(dt: Long, canvas: CanvasProxy): Unit
}
