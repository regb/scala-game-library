package com.regblanc.sgl.test
package core

import sgl._
import sgl.util._

trait AbstractApp extends MainScreenComponent {
  this: GameApp with InputHelpersComponent with GameLoopStatisticsComponent =>

  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}

  override def startingScreen: GameScreen = new MainScreen

}
