package sgl
package proxy

import sgl.util.SchedulerProvider

trait ProxyPlatformProvider extends ProxiedGameApp with SchedulerProvider
  with ProxySystemProvider with ProxyWindowProvider with ProxySchedulerProvider
  with ProxyCanvasProvider
  with ProxyAudioProvider with ProxyLoggingProvider with ProxyJsonProvider {

  val PlatformProxy: PlatformProxy

  override def update(dt: Long, canvas: CanvasProxy): Unit = this.gameLoopStep(dt, Graphics.ProxyCanvas(canvas))

}
