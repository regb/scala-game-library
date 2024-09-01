package com.regblanc.sgl.platformer
package desktop

import core._

import sgl.GameLoopStatisticsComponent
import sgl.util.metrics._
import sgl.awt._
import sgl.awt.util._
import sgl.tiled._


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with VerboseStdErrLoggingProvider
  // Comment out this line and uncomment next one if you do not want instrumentation.
  with GameLoopStatisticsComponent with DefaultInstrumentationProvider
  // with NoInstrumentationProvider
  with TiledMapRendererComponent with TmxJsonParserComponent with LiftJsonProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 800)

  // TODO: The dpi auto-scaling of AWT that is done for -mdpi assets doesn't work with Tiled. Tiled
  // files are all hard-coded to the exact pixel dimensions of the tileset, and thus we need to do
  // some mapping at some point in the rendering of tiled, which is currently not implemented. This
  // is because the tileset is auto-scaled when loaded, and then it's not aligned with the values in the json file.
  // Alternative is to use a file that is not dpi-dependent, and instead is just loaded as is. That could
  // be an alternative loading function.
  // TODO: which one is better? Should we even support multi-dpi? It makes things a lot more complicated, but it just seems
  // to make sense to have it for optimizing memory usage depending on the platform.
  // This settings works because this pretends like the screen is exactly mdpi (160 ppi) and thus no need to scale at all.
  override val ScreenForcePPI = Some(160)

}
