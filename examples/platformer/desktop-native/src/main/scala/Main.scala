package com.regblanc.sgl.platformer
package desktop

import core._

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent}
import sgl.native._
import sgl.native.util._
import sgl.tiled._

/** Wire backend to the App here */
object Main extends NativeApp with AbstractApp
  with InputHelpersComponent with VerboseStdErrLoggingProvider with GameLoopStatisticsComponent 
  with TmxJsonParserComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (TotalWidth, TotalHeight)

}
