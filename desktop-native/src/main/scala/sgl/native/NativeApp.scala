package sgl
package native

import _root_.sgl._
import _root_.sgl.util._

import scalanative.unsafe._

import sdl2.SDL._
import sdl2.Extras._
import gl.GL._
import gl.Extras._

/** Canvas-based native app runner kept as a backend option. */
trait NativeApp extends NativeAppBase
                   with NativeCanvasProvider with NativeInputProvider with NativeAudioProvider
                   with NativeWindowProvider {

  this: Application with LoggingProvider =>

  private var currentFrameCanvas: Option[Graphics.Canvas] = None

  override def withFrameCanvas[A](f: Graphics.Canvas => A): A =
    currentFrameCanvas match {
      case Some(canvas) => f(canvas)
      case None => throw new IllegalStateException("Canvas is only available during a frame")
    }

  override val TargetFps: Option[Int] = Some(60)
  override val windowTitle: String = "Default App"

  override protected def configureGLContext(): Unit = {
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 2)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
  }

  override protected def initializeRenderer(): Unit = {
    glEnable(GL_SCISSOR_TEST)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0f, frameDimension._1, frameDimension._2, 0f, -1f, 1f)
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    glClearColor(0f, 0f, 0f, 1f)
    create()
  }

  override protected def handlePlatformEvent(event: Ptr[SDL_Event]): Unit = handleEvent(event)

  override protected def renderFrame(dt: Long): Unit = {
    val canvas: Graphics.Canvas = new Graphics.NativeCanvas
    currentFrameCanvas = Some(canvas)
    try frame(dt.toDouble)
    finally currentFrameCanvas = None
  }
}
