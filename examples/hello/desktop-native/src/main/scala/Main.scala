package com.regblanc.sgl.test
package desktop

import core._

import sgl.{GameLoopStatisticsComponent}
import sgl.native._
import sgl.native.util._


/** Wire backend to the App here */
object Main extends AbstractApp with NativeApp
  with TraceStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 600)

}
