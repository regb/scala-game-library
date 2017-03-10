package com.regblanc.sgl.snake
package core

import sgl._
import sgl.util._

trait AbstractApp extends MainScreenComponent {
  this: GameApp with InputHelpersComponent =>

  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}

  override def startingScreen: GameScreen = new MainScreen

}
