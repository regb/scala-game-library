package com.regblanc.sgl.test
package desktop

import core._

import sgl.GameLoopStatisticsComponent
import sgl.awt._
import sgl.awt.util._


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with VerboseStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 800)

  override val ScreenForcePPI = Some(160)

}
