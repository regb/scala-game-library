package com.regblanc.sgl.snake
package desktop

import core._

import sgl.GameLoopStatisticsComponent
import sgl.native._
import sgl.native.util._


/** Wire backend to the App here */
object Main extends NativeApp with AbstractApp
  with VerboseStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (TotalWidth, TotalHeight)

}
