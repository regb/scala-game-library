package com.regblanc.sgl.snake
package core

import sgl._
import sgl.proxy._
import sgl.util._

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
