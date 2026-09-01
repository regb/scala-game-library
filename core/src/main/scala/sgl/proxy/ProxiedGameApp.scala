package sgl
package proxy

/** Application lifecycle exposed to proxy-based platform runners. */
trait ProxiedGameApp extends Application with FrameCaptureProvider {

  private var platformFrameCapture: Option[FrameCaptureBatch] = None

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

  /** Java/Kotlin bridge used by proxy render loops before selecting a target. */
  final def beginFrameCaptureForPlatform(): Boolean = {
    if(platformFrameCapture.nonEmpty) throw new IllegalStateException("A proxied frame capture is already active")
    val batch = beginFrameCapture()
    if(batch.requested) platformFrameCapture = Some(batch)
    batch.requested
  }

  /** Completes the active proxy capture with canonical top-left RGBA8 pixels. */
  final def completeFrameCaptureForPlatform(width: Int, height: Int, rgba: Array[Byte]): Unit = {
    platformFrameCapture match {
      case Some(batch) =>
        platformFrameCapture = None
        completeFrameCapture(batch, new CapturedFrame(width, height, rgba))
      case None => throw new IllegalStateException("No proxied frame capture is active")
    }
  }

  final def failFrameCaptureForPlatform(error: Throwable): Unit = {
    platformFrameCapture.foreach(batch => failFrameCapture(batch, error))
    platformFrameCapture = None
  }
}
