package sgl
package native

import sgl.util._

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.image.SDL_image._
import sdl2.image.Extras._

import java.lang.System.nanoTime

trait NativeApp extends GameApp 
                   with NativeGraphicsProvider with NativeInputProvider with NativeAudioProvider
                   with NativeWindowProvider with NativeSystemProvider with GameStateComponent {

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
      IMG_Quit()
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    //TODO: try logical size
    //SDL_RenderSetLogicalSize(renderer, 300, 300)

    this.startup()
    this.resume()

    gameState.newScreen(startingScreen)

    var running = true
    var lastTime: Long = nanoTime
    val canvas = NativeCanvas(this.renderer)
    val event = stackalloc[SDL_Event]

    while(running) {
      val beginTime: Long = nanoTime

      val newTime = nanoTime
      //delta time, in ms (all time measures are in nano)
      val dt = ((newTime - lastTime) / (1000*1000)).toLong
      lastTime = newTime

      while(SDL_PollEvent(event) != 0) {
        if(event.type_ == SDL_QUIT)
          running = false
        else
          handleEvent(event)
      }

      gameLoopStep(dt, canvas)

      SDL_RenderPresent(canvas.renderer)

      val endTime: Long = nanoTime
      val elapsedTime: Long = endTime - beginTime

      val sleepTime: Long = FramePeriod.map(fp => fp - elapsedTime/(1000*1000)).getOrElse(0)

      if(sleepTime > 0) {
        SDL_Delay(sleepTime.toUInt)
      } else if(sleepTime < 0) {
        logger.warning(s"negative sleep time. frame period: $FramePeriod, elapsed time: $elapsedTime.")
      }
    }

    //TODO: for some reason, the app is trapped into a loop when exiting the program
    sys.exit(0)

    logger.info("loop exit")
    IMG_Quit()
    logger.info("IMG quit")
    SDL_DestroyRenderer(renderer)
    logger.info("renderer destroyed")
    SDL_DestroyWindow(window)
    logger.info("window destroyed")
    SDL_Quit()
    logger.info("sdl quit")
  }

}
