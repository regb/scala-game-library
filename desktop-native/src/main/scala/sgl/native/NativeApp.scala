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


  //TODO: way to specify initial position of the window

  def main(args: Array[String]): Unit = {
    println("Hello SGL Native")

    if(SDL_Init(INIT_VIDEO) != 0) {
      println("Failed to init SDL: " + SDL_GetError())
      sys.exit()
    }

    val window = SDL_CreateWindow(c"Default App", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, frameDimension._1, frameDimension._2, WINDOW_SHOWN)
    if(window == null) {
      println("Failed to create a window: " + SDL_GetError())
      SDL_Quit()
      sys.exit()
    }


    this.renderer = SDL_CreateRenderer(window, -1, VSYNC)
    if(this.renderer == null) {
      //TODO: SDL_DestroyWindow
      println("Failed to create a renderer: " + SDL_GetError())
      SDL_Quit()
      sys.exit()
    }

    this.startup()
    this.resume()

    gameLoop.init()
    gameLoop.loop()
  }

}
