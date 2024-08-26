package com.regblanc.sgl.test
package core

import sgl._
import sgl.proxy._
import sgl.util._

trait AbstractApp extends GameApp with MainScreenComponent {
  this: SchedulerProvider =>

  override def startingScreen: GameScreen = LoadingScreen

}

object Wiring {

  def wire(platformProxy: PlatformProxy): ProxiedGameApp = {
    new AbstractApp with ProxyPlatformProvider {
      override val PlatformProxy: PlatformProxy = platformProxy
    }
  }
}
