package com.regblanc.sgl.snake
package desktop

import core._

import sgl.{GameLoopStatisticsComponent}
import sgl.awt._
import sgl.awt.util._


/** Wire backend to the App here */
object Main extends AWTApp with AbstractApp
  with VerboseStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (TotalWidth, TotalHeight)

}
