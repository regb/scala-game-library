package sgl
package native

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._

trait NativeWindowProvider extends WindowProvider {
  this: GameStateComponent with NativeGraphicsProvider =>

  val frameDimension: (Int, Int)

  //TODO: provide a WindowDimension object, with either fixed width/height or FullScreen
  //abstract class WindowDimension
  //case class FixedWindowDimension(width: Int, height: Int)
  //case object FullScreen
  //case class ResizableWIndowDimension(width: Int, height: Int)
  //val WindowDimension: WindowDimension

  override def WindowWidth: Int = frameDimension._1
  override def WindowHeight: Int = frameDimension._2

  override def dpi: Int = {
    val ddpi: Ptr[CFloat] = stackalloc[CFloat]
    //val hdpi: Ptr[CFloat] = stackalloc[CFloat]
    //val vdpi: Ptr[CFloat] = stackalloc[CFloat]
    //SDL_GetDisplayDPI(0, ddpi, hdpi, vdpi)
    SDL_GetDisplayDPI(0, ddpi, null, null)
    //println("ddpi: " + !ddpi + ". hdpi: " + !hdpi + ". vdpi: " + !vdpi)
    Math.round(!ddpi)
  }

  override def density: Float = 1f

}
