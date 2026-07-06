package com.regblanc.sgl.hello
package core

import _root_.sgl._
import _root_.sgl.proxy._
import _root_.sgl.util._

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
