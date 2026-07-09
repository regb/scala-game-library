package sgl.examples.screen2d.menu
package core

import _root_.sgl._

trait AbstractApp extends ScreensComponent {
  this: Screen2DGameApp =>

  override def startingScreen: GameScreen = new LevelsScreen

}
