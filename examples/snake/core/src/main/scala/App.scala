package com.regblanc.sgl.snake
package core

import sgl._
import sgl.util._

trait AbstractApp extends MainScreenComponent {
  this: GameApp with InputHelpersComponent =>

  override def startingScreen: GameScreen = new MainScreen

}
