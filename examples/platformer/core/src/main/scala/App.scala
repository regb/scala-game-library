package com.regblanc.sgl.platformer
package core

import sgl._
import sgl.util._
import sgl.tiled._

trait AbstractApp extends MainScreenComponent {
  this: GameApp with InputHelpersComponent with GameLoopStatisticsComponent
  with TiledMapRendererComponent with TmxJsonParserComponent =>

  override def startingScreen: GameScreen = new MainScreen

}
