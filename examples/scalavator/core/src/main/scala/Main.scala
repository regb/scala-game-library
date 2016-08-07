package com.regblanc.scalavator
package core

import sgl._
import sgl.awt._
import scene._
import util._

trait AbstractApp extends MainScreenComponent {
  this: GraphicsProvider with InputProvider with WindowProvider with AudioProvider
  with GameScreensComponent with SystemProvider
  with GameLoopComponent with SceneComponent with LoggingProvider =>

  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}

  override def startingScreen: GameScreen = new MainScreen

}


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp with SceneComponent with VerboseStdErrLoggingProvider {

  override val Fps = Some(60)

  override val frameDimension = Some((400, 650))

}
