package sgl
package proxy

import sgl.util.SchedulerProvider

/** Base class that initializes the platform before game traits. This avoids
  * cake-initialization nulls when a game creates provider-backed fields.
  */
abstract class WiredProxyPlatform(final override val PlatformProxy: PlatformProxy) extends ProxyPlatformProvider

trait ProxyPlatformProvider extends ProxiedGameApp with SchedulerProvider
  with ProxySystemProvider with ProxyWindowProvider with ProxySchedulerProvider
  with ProxyCanvasProvider
  with ProxyAudioProvider with ProxyLoggingProvider with ProxyJsonProvider {

  val PlatformProxy: PlatformProxy

  private var activeFrameCanvas: Option[Graphics.Canvas] = None

  override def update(dt: Long, canvas: CanvasProxy): Unit = {
    if(activeFrameCanvas.nonEmpty) throw new IllegalStateException("Proxied frame update is already active")
    val frameCanvas = Graphics.ProxyCanvas(canvas)
    activeFrameCanvas = Some(frameCanvas)
    try frame(dt.toDouble / 1000.0)
    finally activeFrameCanvas = None
  }

  override def withFrameCanvas[A](f: Graphics.Canvas => A): A = activeFrameCanvas match {
    case Some(canvas) => f(canvas)
    case None => throw new IllegalStateException("Frame canvas is only available during a proxied update")
  }
}
