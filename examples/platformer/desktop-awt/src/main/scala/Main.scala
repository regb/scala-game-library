package com.regblanc.sgl.platformer
package desktop

import core._

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent}
import sgl.util.metrics._
import sgl.awt._
import sgl.awt.util._
import sgl.tiled._


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with InputHelpersComponent with VerboseStdErrLoggingProvider
  // Comment out this line and uncomment next one if you do not want instrumentation.
  with GameLoopStatisticsComponent with DefaultInstrumentationProvider
  // with NoInstrumentationProvider
  with TiledMapRendererComponent with TmxJsonParserComponent with LiftJsonProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 800)

}
