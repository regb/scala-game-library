package sgl
package awt

import util._

trait AWTApp extends GameApp 
                with AWTGraphicsProvider with AWTInputProvider with AWTAudioProvider
                with AWTWindowProvider with GameLoopComponent
                with AWTSystemProvider with GameScreensComponent {

  this: LoggingProvider =>

  def main(args: Array[String]): Unit = {
    this.startup()
    this.resume()
  }
}
