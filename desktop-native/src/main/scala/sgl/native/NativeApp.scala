package sgl
package native

import sgl.util._

import scalanative.native._

import SDL._
import SDLExtra._

trait NativeApp extends GameApp 
                   with NativeGraphicsProvider with NativeInputProvider with NativeAudioProvider
                   with NativeWindowProvider with PollingGameLoopProvider
                   with NativeSystemProvider with GameStateComponent {

  this: LoggingProvider =>

  def main(args: Array[String]): Unit = {
    println("Hello SGL Native")

    SDL_Init(INIT_VIDEO)
    val window = SDL_CreateWindow(c"Default App", 0, 0, 500, 500, WINDOW_SHOWN)
    this.renderer = SDL_CreateRenderer(window, -1, VSYNC)

    this.startup()
    this.resume()

    gameLoop.init()
    gameLoop.loop()
  }

}
