package com.regblanc.sgl.snake
package core

import _root_.sgl._
import _root_.sgl.proxy._

trait AbstractApp extends MainScreenComponent {
  this: GameApp =>

  override def startingScreen: GameScreen = new MainScreen

}

object Wiring {

  def wire(platformProxy: PlatformProxy): ProxiedGameApp = {
    new AbstractApp with ProxyPlatformProvider {
      override val PlatformProxy: PlatformProxy = platformProxy
    }
  }
}
