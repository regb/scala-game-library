package sgl.examples.screen2d.platformer
package core

import sgl._
import sgl.util._
import sgl.util.metrics.InstrumentationProvider
import sgl.tiled._

trait AbstractApp extends MainScreenComponent with AssetsProvider {
  this: Screen2DGameApp with InstrumentationProvider
  with TiledMapRendererComponent with TmxJsonParserComponent with LoggingProvider =>

  override def startingScreen: GameScreen = new MainScreen

}
