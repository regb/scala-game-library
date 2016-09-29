package sgl
package android

import util._

trait AndroidApp extends GameApp
                    with AndroidGraphicsProvider with AndroidInputProvider with AndroidAudioProvider
                    with AndroidWindowProvider with GameLoopComponent
                    with AndroidSystemProvider with GameScreensComponent {

  this: LoggingProvider =>

}
