package com.regblanc.sgl.platformer
package desktop

import core._

import sgl.GameLoopStatisticsComponent
import sgl.util._
import sgl.util.metrics._
import sgl.native._
import sgl.native.util._
import sgl.tiled._

/** Wire backend to the App here */
object Main extends AbstractApp with NativeApp
  with VerboseStdErrLoggingProvider
  with GameLoopStatisticsComponent with DefaultInstrumentationProvider
  with TiledMapRendererComponent with TmxJsonParserComponent with JsonProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (600, 800)

  override val Json: Json = ???

}
