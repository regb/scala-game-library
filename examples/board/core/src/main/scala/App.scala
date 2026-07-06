package com.regblanc.sgl.board
package core

import _root_.sgl._

trait AbstractApp extends ScreensComponent {
  this: GameApp with ViewportComponent =>

  override def startingScreen: GameScreen = new BoardScreen

}
