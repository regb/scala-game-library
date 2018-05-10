package sgl
package native

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._

trait NativeWindowProvider extends WindowProvider {
  this: GameStateComponent with NativeGraphicsProvider =>

  val frameDimension: (Int, Int)

  class NativeWindow extends AbstractWindow {
    override def width: Int = frameDimension._1
    override def height: Int = frameDimension._2

    // TODO: should refresh when Window is resized or dpis changes.
    private var _xppi: Float = 0f
    private var _yppi: Float = 0f
    private var _ppi: Float = 0f
    private def computePPIs(): Unit = {
      val ddpi: Ptr[CFloat] = stackalloc[CFloat]
      val hdpi: Ptr[CFloat] = stackalloc[CFloat]
      val vdpi: Ptr[CFloat] = stackalloc[CFloat]
      SDL_GetDisplayDPI(0, ddpi, hdpi, vdpi)
      _xppi = !hdpi
      _yppi = !vdpi
      _ppi = !ddpi
    }

    override def xppi: Float = if(_xppi != 0f) _xppi else {
      computePPIs()
      _xppi
    }
    override def yppi: Float = if(_yppi != 0f) _yppi else {
      computePPIs()
      _yppi
    }
    override def ppi: Float = if(_ppi != 0f) _ppi else {
      computePPIs()
      _ppi
    }
  }
  type Window = NativeWindow
  override val Window = new NativeWindow

  ///** The name of the window */
  //val windowTitle: String

  //TODO: provide a WindowDimension object, with either fixed width/height or FullScreen
  //abstract class WindowDimension
  //case class FixedWindowDimension(width: Int, height: Int)
  //case object FullScreen
  //case class ResizableWIndowDimension(width: Int, height: Int)
  //val WindowDimension: WindowDimension


}
