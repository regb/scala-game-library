package sgl
package native

import _root_.sgl.{Application, Input}
import _root_.sgl.util.LoggingProvider

import scalanative.unsafe._

import sdl2.SDL._
import sdl2.Extras._

trait NativeOpenGLApp extends NativeAppBase
    with NativeOpenGLProvider
    with NativeInputProvider
    with NativeAudioProvider
    with NativeWindowProvider {
  this: Application with LoggingProvider =>

  override val windowTitle: String = "SGL Native OpenGL App"

  override def runOnOpenGLThread(task: => Unit): Unit = super[NativeAppBase].runOnOpenGLThread(task)

  def viewportWidth: Int = frameDimension._1
  def viewportHeight: Int = frameDimension._2

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

  override protected def handlePlatformEvent(event: Ptr[SDL_Event]): Unit = {
    handleEvent(event)
  }

  override protected def renderFrame(dt: Long): Unit = {
    frame(dt.toDouble / 1000.0)
  }

  override protected def shutdownRenderer(): Unit = {
    dispose()
  }
}
