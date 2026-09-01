package sgl.native

import sgl.{Application, OpenGLCanvasProvider}
import sgl.util.LoggingProvider

import scalanative.unsafe.Ptr

import sdl2.SDL._
import sdl2.Extras._

/** Canvas application rendered by the portable OpenGL Canvas implementation. */
trait NativeApp extends NativeAppBase
    with NativeOpenGLProvider
    with OpenGLCanvasProvider
    with NativeInputProvider
    with NativeAudioProvider
    with NativeWindowProvider {
  this: Application with LoggingProvider =>

  override val TargetFps: Option[Int] = Some(60)
  override val windowTitle: String = "Default App"

  override def runOnOpenGLThread(task: => Unit): Unit = super[NativeAppBase].runOnOpenGLThread(task)

  override protected def configureGLContext(): Unit = {
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_ES.toInt)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 0)
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
  }

  override protected def initializeRenderer(): Unit = {
    create()
    resize(frameDimension._1, frameDimension._2)
  }

  override protected def handlePlatformEvent(event: Ptr[SDL_Event]): Unit = handleEvent(event)

  override protected def renderFrame(dt: Double): Unit = {
    Audio.update()
    frame(dt)
  }

  override protected def shutdownRenderer(): Unit = {
    try dispose()
    finally {
      try disposeOpenGLCanvas()
      finally Audio.dispose()
    }
  }
}
