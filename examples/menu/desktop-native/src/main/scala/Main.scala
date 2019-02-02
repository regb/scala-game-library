package com.regblanc.sgl.menu
package desktop

import core._

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent, ViewportComponent}
import sgl.native._
import sgl.native.util._
import sgl.scene._


/** Wire backend to the App here */
object Main extends AbstractApp with NativeApp
  with InputHelpersComponent with TraceStdErrLoggingProvider with GameLoopStatisticsComponent
  with SceneComponent with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 600)

}
