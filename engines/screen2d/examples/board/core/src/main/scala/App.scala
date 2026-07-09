package sgl.examples.screen2d.board
package core

import _root_.sgl._

trait AbstractApp extends ScreensComponent {
  this: Screen2DGameApp =>

  override def startingScreen: GameScreen = new BoardScreen

}
