package com.regblanc.sgl.menu
package core

import _root_.sgl._

trait AbstractApp extends ScreensComponent {
  this: GameApp =>

  override def startingScreen: GameScreen = new LevelsScreen

}
