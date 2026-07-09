package sgl.examples.screen2d.board
package desktop

import core._

import sgl.{DesktopSystemProvider, GameLoopStatisticsComponent, ViewportComponent}
import sgl.awt._
import sgl.desktop.util._

/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp
  with DesktopSystemProvider
  with VerboseStdErrLoggingProvider
  with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (450, 780)

}
