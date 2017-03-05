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


  /** Initial position of the window
    *
    * If None, then the window is centered by default. Else, the
    * tuple is the top-left (x,y) coordinate
    */
  val WindowInitialPosition: Option[(Int, Int)] = None

  def main(args: Array[String]): Unit = {
    println("Hello SGL Native")

    if(SDL_Init(INIT_VIDEO) != 0) {
      println("Failed to init SDL: " + SDL_GetError())
      sys.exit()
    }

    val (x,y) = WindowInitialPosition.getOrElse((SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED))

    val window = SDL_CreateWindow(c"Default App", x, y, frameDimension._1, frameDimension._2, SDL_WINDOW_SHOWN)
    if(window == null) {
      println("Failed to create a window: " + SDL_GetError())
      SDL_Quit()
      sys.exit()
    }


    this.renderer = SDL_CreateRenderer(window, -1, VSYNC)
    if(this.renderer == null) {
      println("Failed to create a renderer: " + SDL_GetError())
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    this.startup()
    this.resume()

    gameLoop.init()
    gameLoop.loop()

    SDL_DestroyWindow(window)
    SDL_Quit()
  }

}
