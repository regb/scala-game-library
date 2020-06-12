package com.regblanc.sgl.board
package core

import sgl._
import sgl.util._
import sgl.scene._
import sgl.scene.ui._

trait AbstractApp extends ScreensComponent {
  this: GameApp with ViewportComponent =>

  override def startingScreen: GameScreen = new BoardScreen

}
