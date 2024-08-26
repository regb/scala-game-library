package com.regblanc.sgl.board
package desktop

import core._

import sgl.{GameLoopStatisticsComponent, ViewportComponent}
import sgl.awt._
import sgl.awt.util._

/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with VerboseStdErrLoggingProvider
  with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (450, 780)

}
