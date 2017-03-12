package sgl
package native

import sgl.util._

import scalanative.native._

import sdl2.SDL2._
import sdl2.Extras._
import sdl2.image.SDL2_image._
import sdl2.image.Extras._

trait NativeApp extends GameApp 
                   with NativeGraphicsProvider with NativeInputProvider with NativeAudioProvider
                   with NativeWindowProvider with PollingGameLoopProvider
                   with NativeSystemProvider with GameStateComponent {

  this: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("native.main")

  /** Initial position of the window
    *
    * If None, then the window is centered by default. Else, the
    * tuple is the top-left (x,y) coordinate
    */
  val WindowInitialPosition: Option[(Int, Int)] = None

  def main(args: Array[String]): Unit = {
    if(SDL_Init(SDL_INIT_VIDEO) != 0) {
      logger.error("Failed to init SDL: " + fromCString(SDL_GetError()))
      sys.exit()
    }

    val (x,y) = WindowInitialPosition.getOrElse((SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED))

    val window = SDL_CreateWindow(c"Default App", x, y, frameDimension._1, frameDimension._2, SDL_WINDOW_SHOWN)
    if(window == null) {
      logger.error("Failed to create a window: " + fromCString(SDL_GetError()))
      SDL_Quit()
      sys.exit()
    }

    val imgFlags = IMG_INIT_PNG
    if(IMG_Init(imgFlags) != imgFlags) {
      logger.error("Failed to initialize SDL_image: " + IMG_GetError())
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    this.renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_PRESENTVSYNC)
    if(this.renderer == null) {
      logger.error("Failed to create a renderer: " + fromCString(SDL_GetError()))
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    this.startup()
    this.resume()

    gameLoop.init()
    gameLoop.loop()

    IMG_Quit()
    SDL_DestroyWindow(window)
    SDL_Quit()
  }

}
