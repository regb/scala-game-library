package com.regblanc.sgl.menu
package desktop

import core._

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent, ViewportComponent}
import sgl.awt._
import sgl.awt.util._
import sgl.scene._

/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with InputHelpersComponent with VerboseStdErrLoggingProvider with GameLoopStatisticsComponent
  with SceneComponent with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (400, 600)

}
