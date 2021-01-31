package sgl
package native

import sgl.util._

import scalanative.unsafe._
import scalanative.unsigned._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.image.SDL_image._
import sdl2.image.Extras._
import gl.GL._
import gl.Extras._

import java.lang.System.nanoTime

trait NativeApp extends GameApp 
                   with NativeGraphicsProvider with NativeInputProvider with NativeAudioProvider
                   with NativeWindowProvider with NativeSystemProvider with GameStateComponent 
                     // TODO: use thread-based scheduler
                   with SingleThreadSchedulerProvider {

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

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 2)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)

    val (x,y) = WindowInitialPosition.getOrElse((SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED))

    val window = SDL_CreateWindow(c"Default App", x, y, frameDimension._1, frameDimension._2, SDL_WINDOW_OPENGL)
    if(window == null) {
      logger.error("Failed to create a window: " + fromCString(SDL_GetError()))
      SDL_Quit()
      sys.exit()
    }

    val glContext = SDL_GL_CreateContext(window)
    if (glContext == null) {
      logger.error("Could not create OpenGL context: " + fromCString(SDL_GetError()))
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }
    SDL_GL_SetSwapInterval(1)

    val imgFlags = IMG_INIT_PNG
    if(IMG_Init(imgFlags) != imgFlags) {
      logger.error("Failed to initialize SDL_image: " + IMG_GetError())
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    //this.renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_PRESENTVSYNC)
    //if(this.renderer == null) {
    //  logger.error("Failed to create a renderer: " + fromCString(SDL_GetError()))
    //  IMG_Quit()
    //  SDL_DestroyWindow(window)
    //  SDL_Quit()
    //  sys.exit()
    //}

    glEnable(GL_SCISSOR_TEST)
    //seems that we only need to active texture-2d during rendering of bitmaps
    //glEnable(GL_TEXTURE_2D)

    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0f, frameDimension._1, frameDimension._2, 0f, -1f, 1f)

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()

    glClearColor(0f, 0f, 0f, 1f)

    gameState.newScreen(startingScreen)

    val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true
    var lastTime: Long = nanoTime
    val canvas: Graphics.Canvas = new Graphics.NativeCanvas
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

      //SDL_RenderPresent(canvas.renderer)
      SDL_GL_SwapWindow(window)

      val currentTime: Long = nanoTime
      val timeForScheduler: Long = targetFramePeriod.map(fp => fp - (currentTime - beginTime)/(1000l*1000l)).getOrElse(10l)
      Scheduler.run(timeForScheduler)

      val endTime: Long = nanoTime
      val elapsedTime: Long = endTime - beginTime

      val sleepTime: Long = targetFramePeriod.map(fp => fp - elapsedTime/(1000l*1000l)).getOrElse(0)

      if(sleepTime > 0) {
        SDL_Delay(sleepTime.toUInt)
      } else if(sleepTime < 0) {
        logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $elapsedTime.")
      }
    }

    IMG_Quit()
    //SDL_DestroyRenderer(renderer)
    SDL_DestroyWindow(window)
    SDL_Quit()
  }

}
