package com.regblanc.scalavator
package core

import sgl._
import scene._
import util._

trait AbstractApp extends MainScreenComponent with Lifecycle {
  this: GraphicsProvider with InputProvider with WindowProvider with AudioProvider
  with GameStateComponent with SystemProvider
  with GameLoopProvider with SceneComponent with LoggingProvider =>

  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}

  override def startingScreen: GameScreen = new MainScreen

}
