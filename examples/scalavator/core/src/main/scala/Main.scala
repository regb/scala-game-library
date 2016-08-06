package com.regblanc.scalavator
package core

import sgl._
import sgl.awt._
import analytics._

trait AbstractApp extends MainScreenComponent {
  this: GraphicsProvider with InputProvider with WindowProvider with AudioProvider
  with GameScreensComponent with SystemProvider
  with GameLoopComponent =>

  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}

  override def startingScreen: GameScreen = new MainScreen

}


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp {

  override val frameDimension = Some((400, 600))

}
