package sgl
package awt

import sgl.util._

trait AWTApp extends GameApp 
                with AWTGraphicsProvider with AWTInputProvider with AWTAudioProvider
                with AWTWindowProvider with ThreadBasedGameLoopProvider
                with AWTSystemProvider with GameStateComponent {

  this: LoggingProvider =>

  def main(args: Array[String]): Unit = {

    this.gamePanel = new GamePanel
    this.applicationFrame = new ApplicationFrame(gamePanel)

    this.startup()
    this.resume()
  }
}
