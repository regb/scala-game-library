package sgl
package android

import util._

trait AndroidApp extends GameApp
                    with AndroidGraphicsProvider with AndroidInputProvider with AndroidAudioProvider
                    with AndroidWindowProvider with ThreadBasedGameLoopProvider
                    with AndroidSystemProvider with GameStateComponent {

  this: LoggingProvider =>

}
