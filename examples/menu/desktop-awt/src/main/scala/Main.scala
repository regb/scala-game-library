package com.regblanc.sgl.menu
package desktop

import core._

import sgl.{GameLoopStatisticsComponent, ViewportComponent}
import sgl.awt._
import sgl.awt.util._
import sgl.scene._
import sgl.scene.ui._

/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with VerboseStdErrLoggingProvider
  with SceneComponent with PopupsComponent with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (400, 600)

}
