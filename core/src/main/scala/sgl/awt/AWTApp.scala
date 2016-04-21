package sgl
package awt


trait AWTApp extends GameApp 
                with AWTGraphicsProvider with AWTInputProvider with AWTAudioProvider
                with AWTWindowProvider with GameLoopComponent
                with AWTSystemProvider with GameScreensComponent {

  def main(args: Array[String]): Unit = {
    this.startup()
    this.resume()
  }
}
