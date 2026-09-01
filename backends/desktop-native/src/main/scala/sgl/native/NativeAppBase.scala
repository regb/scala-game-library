package sgl
package native

import _root_.sgl.util._

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.stdlib

import gl.GL._
import gl.Extras._

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
trait NativeAppBase extends NativeSystemProvider with NativeRenderThreadDispatcher with SingleThreadSchedulerProvider with FrameCaptureProvider {
  this: LoggingProvider =>

  private implicit val LogTag: NativeAppBase.this.Logger.Tag = Logger.Tag("native.main")

  val TargetFps: Option[Int] = Some(60)
  val frameDimension: (Int, Int)
  val windowTitle: String = "SGL Native App"
  val WindowInitialPosition: Option[(Int, Int)] = None

  protected def configureGLContext(): Unit
  protected def initializeRenderer(): Unit
  protected def renderFrame(dt: Double): Unit
  protected def handlePlatformEvent(event: Ptr[SDL_Event]): Unit = ()
  protected def shutdownRenderer(): Unit = ()

  private val renderThreadTasks = new ConcurrentLinkedQueue[() => Unit]
  private val renderTaskLock = new Object
  @volatile private var acceptingRenderTasks = true

  override def runOnOpenGLThread(task: => Unit): Unit = renderTaskLock.synchronized {
    if(!acceptingRenderTasks) throw new IllegalStateException("The OpenGL render thread has stopped")
    renderThreadTasks.add(() => task)
  }

  private def runPendingRenderThreadTasks(): Unit = {
    var task = renderThreadTasks.poll()
    while(task != null) {
      task()
      task = renderThreadTasks.poll()
    }
  }

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

    var rendererInitialized = false
    try {
      rendererInitialized = true
      initializeRenderer()

      val targetFramePeriod: Option[Long] = TargetFps.map(Application.framePeriodMillis)
    var running = true
    var lastTime: Long = nanoTime
    val event = stackalloc[SDL_Event]()

    while(running) {
      val beginTime: Long = nanoTime
      val newTime = nanoTime
      val dt = (newTime - lastTime).toDouble / 1000000000.0
      lastTime = newTime

      while(SDL_PollEvent(event) != 0) {
        if(event.type_ == SDL_QUIT) running = false
        else handlePlatformEvent(event)
      }

      runPendingRenderThreadTasks()
      val captureBatch = beginFrameCapture()
      try {
        renderFrame(dt)
        completeFrameCapture(captureBatch, captureOpenGLFrame())
      } catch {
        case error: Throwable =>
          failFrameCapture(captureBatch, error)
          throw error
      }
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

    } finally {
      runPendingRenderThreadTasks()
      try {
        if(rendererInitialized) shutdownRenderer()
      } finally {
        renderTaskLock.synchronized { acceptingRenderTasks = false }
        runPendingRenderThreadTasks()
        IMG_Quit()
        SDL_GL_DeleteContext(glContext)
        SDL_DestroyWindow(window)
        SDL_Quit()
      }
    }
  }

  private def captureOpenGLFrame(): CapturedFrame = {
    val width = frameDimension._1
    val height = frameDimension._2
    val rowBytes = width * CapturedFrame.BytesPerPixel
    val size = rowBytes * height
    val source = stdlib.malloc(size.toUInt).asInstanceOf[Ptr[Byte]]
    if(source == null) throw new OutOfMemoryError(s"Could not allocate $size bytes for frame capture")

    try {
      glReadPixels(0, 0, width.toUInt, height.toUInt, GL_RGBA, GL_UNSIGNED_BYTE, source)
      val rgba = new Array[Byte](size)
      var destinationY = 0
      while(destinationY < height) {
        val sourceY = height - 1 - destinationY
        var x = 0
        while(x < rowBytes) {
          rgba(destinationY * rowBytes + x) = !(source + sourceY * rowBytes + x)
          x += 1
        }
        destinationY += 1
      }
      new CapturedFrame(width, height, rgba)
    } finally stdlib.free(source)
  }
}
