package sgl
package native

import _root_.sgl.util._

import scalanative.unsafe._
import scalanative.unsigned._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.image.SDL_image._
import sdl2.image.Extras._

import java.lang.System.nanoTime
import java.util.concurrent.ConcurrentLinkedQueue

/** Shared SDL/native application runner.
  *
  * Rendering backends customize the GL context attributes, renderer
  * initialization, event handling, and per-frame rendering while sharing SDL
  * setup, image/audio initialization, timing, scheduler, and shutdown.
  */
trait NativeAppBase extends NativeSystemProvider with NativeRenderThreadDispatcher with SingleThreadSchedulerProvider {
  this: LoggingProvider =>

  private implicit val LogTag: NativeAppBase.this.Logger.Tag = Logger.Tag("native.main")

  val TargetFps: Option[Int] = Some(60)
  val frameDimension: (Int, Int)
  val windowTitle: String = "SGL Native App"
  val WindowInitialPosition: Option[(Int, Int)] = None

  protected def configureGLContext(): Unit
  protected def initializeRenderer(): Unit
  protected def renderFrame(dt: Long): Unit
  protected def handlePlatformEvent(event: Ptr[SDL_Event]): Unit = ()
  protected def shutdownRenderer(): Unit = ()

  private val renderThreadTasks = new ConcurrentLinkedQueue[() => Unit]

  override def runOnOpenGLThread(task: => Unit): Unit = {
    renderThreadTasks.add(() => task)
  }

  private def runPendingRenderThreadTasks(): Unit = {
    var task = renderThreadTasks.poll()
    while(task != null) {
      task()
      task = renderThreadTasks.poll()
    }
  }

  private def framePeriod(fps: Int): Long = 1000L / fps

  def main(args: Array[String]): Unit = {
    if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO) != 0) {
      logger.error("Failed to init SDL: " + fromCString(SDL_GetError()))
      sys.exit()
    }

    configureGLContext()

    val (x, y) = WindowInitialPosition.getOrElse((SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED))
    val window = Zone.acquire { implicit z =>
      SDL_CreateWindow(toCString(windowTitle), x, y, frameDimension._1, frameDimension._2, SDL_WINDOW_OPENGL)
    }
    if(window == null) {
      logger.error("Failed to create a window: " + fromCString(SDL_GetError()))
      SDL_Quit()
      sys.exit()
    }

    val glContext = SDL_GL_CreateContext(window)
    if(glContext == null) {
      logger.error("Could not create OpenGL context: " + fromCString(SDL_GetError()))
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }
    SDL_GL_SetSwapInterval(1)

    val imgFlags = IMG_INIT_PNG
    if(IMG_Init(imgFlags) != imgFlags) {
      logger.error("Failed to initialize SDL_image: " + IMG_GetError())
      SDL_GL_DeleteContext(glContext)
      SDL_DestroyWindow(window)
      SDL_Quit()
      sys.exit()
    }

    initializeRenderer()

    val targetFramePeriod: Option[Long] = TargetFps.map(framePeriod)
    var running = true
    var lastTime: Long = nanoTime
    val event = stackalloc[SDL_Event]()

    while(running) {
      val beginTime: Long = nanoTime
      val newTime = nanoTime
      val dt = ((newTime - lastTime) / (1000L * 1000L)).toLong
      lastTime = newTime

      while(SDL_PollEvent(event) != 0) {
        if(event.type_ == SDL_QUIT) running = false
        else handlePlatformEvent(event)
      }

      runPendingRenderThreadTasks()
      renderFrame(dt)
      SDL_GL_SwapWindow(window)

      val currentTime: Long = nanoTime
      val timeForScheduler: Long = targetFramePeriod.map(fp => fp - (currentTime - beginTime)/(1000L*1000L)).getOrElse(10L)
      Scheduler.run(timeForScheduler)

      val elapsedTime: Long = nanoTime - beginTime
      val sleepTime: Long = targetFramePeriod.map(fp => fp - elapsedTime/(1000L*1000L)).getOrElse(0L)

      if(sleepTime > 0) {
        SDL_Delay(sleepTime.toUInt)
      } else if(sleepTime < 0) {
        logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $elapsedTime.")
      }
    }

    shutdownRenderer()
    IMG_Quit()
    SDL_GL_DeleteContext(glContext)
    SDL_DestroyWindow(window)
    SDL_Quit()
  }
}
