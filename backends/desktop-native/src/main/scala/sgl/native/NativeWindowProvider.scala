package sgl
package native

import _root_.sgl._
import _root_.sgl.util.Loader
import scalanative.unsafe._

import sdl2.SDL._
import sdl2.Extras._

trait NativeWindowProvider extends WindowProvider {

  val frameDimension: (Int, Int)

  val ScreenForcePPI: scala.Option[Float] = None

  private object NativeWindow {
    /** One pixel per density-independent pixel. */
    val DefaultPpi = 160f
  }

  class NativeWindow extends AbstractWindow {
    override def width: Int = frameDimension._1
    override def height: Int = frameDimension._2

    override def copyText(text: String): Unit = Zone.acquire { implicit zone =>
      val _ = SDL_SetClipboardText(toCString(text, java.nio.charset.StandardCharsets.UTF_8))
    }
    override def pasteText(): Loader[String] = {
      val text = SDL_GetClipboardText()
      if(text == null) super.pasteText()
      else {
        val value = fromCString(text, java.nio.charset.StandardCharsets.UTF_8)
        SDL_free(text)
        Loader.successful(value)
      }
    }

    // TODO: should refresh when Window is resized or dpis changes.
    private var _xppi: Float = 0f
    private var _yppi: Float = 0f
    private var _ppi: Float = 0f
    private def computePPIs(): Unit = {
      val ddpi: Ptr[CFloat] = stackalloc[CFloat]()
      val hdpi: Ptr[CFloat] = stackalloc[CFloat]()
      val vdpi: Ptr[CFloat] = stackalloc[CFloat]()
      // Displays without a physical size, as virtual ones, do not have a DPI.
      val known = SDL_GetDisplayDPI(0, ddpi, hdpi, vdpi) == 0
      def orDefault(ppi: Float): Float = if(known && ppi > 0f) ppi else NativeWindow.DefaultPpi
      _xppi = orDefault(!hdpi)
      _yppi = orDefault(!vdpi)
      _ppi = orDefault(!ddpi)
    }

    override def xppi: Float = ScreenForcePPI.getOrElse {
      if(_xppi == 0f) computePPIs()
      _xppi
    }
    override def yppi: Float = ScreenForcePPI.getOrElse {
      if(_yppi == 0f) computePPIs()
      _yppi
    }

    // TODO: rounding?
    override def logicalPpi: Float = ScreenForcePPI.getOrElse {
      if(_ppi == 0f) computePPIs()
      _ppi
    }
  }
  type Window = NativeWindow
  override val Window: NativeWindow = new NativeWindow

  ///** The name of the window */
  //val windowTitle: String

  //TODO: provide a WindowDimension object, with either fixed width/height or FullScreen
  //abstract class WindowDimension
  //case class FixedWindowDimension(width: Int, height: Int)
  //case object FullScreen
  //case class ResizableWIndowDimension(width: Int, height: Int)
  //val WindowDimension: WindowDimension


}
