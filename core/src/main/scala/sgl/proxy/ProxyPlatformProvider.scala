package sgl
package proxy

import sgl.util.SchedulerProvider
import sgl.util.NoLoggingProvider

trait ProxyPlatformProvider extends ProxiedGameApp with SchedulerProvider
  with ProxySystemProvider with ProxyWindowProvider with ProxySchedulerProvider 
  with ProxyGraphicsProvider
  with FakeAudioProvider with NoLoggingProvider {

  val PlatformProxy: PlatformProxy

  override def update(dt: Long, canvas: CanvasProxy): Unit = this.gameLoopStep(dt, Graphics.ProxyCanvas(canvas))

}
