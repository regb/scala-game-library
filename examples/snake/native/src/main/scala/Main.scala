package com.regblanc.sgl.snake
package desktop

import core._

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent}
import sgl.native._
import sgl.native.util._


/** Wire backend to the App here */
object Main extends AbstractApp with NativeApp
  with InputHelpersComponent with VerboseStdErrLoggingProvider with GameLoopStatisticsComponent {

  override val Fps = Some(60)

  override val frameDimension = (TotalWidth, TotalHeight)

}
