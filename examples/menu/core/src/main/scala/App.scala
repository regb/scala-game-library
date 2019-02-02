package com.regblanc.sgl.menu
package core

import sgl._
import sgl.util._
import sgl.scene._

trait AbstractApp extends ScreensComponent {
  this: GameApp with InputHelpersComponent with GameLoopStatisticsComponent
  with ViewportComponent with SceneComponent =>

  override def startingScreen: GameScreen = new LevelsScreen

}
