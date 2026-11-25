package sgl.lwjgl

import sgl.WindowProvider
import sgl.util.Loader

/** Window dimensions shared by visible and headless LWJGL contexts. */
trait LwjglWindowProvider extends WindowProvider {
  val frameDimension: (Int, Int)
  val ScreenForcePPI: Option[Float] = None

  private var framebufferWidth: Int = 0
  private var framebufferHeight: Int = 0

  protected final def setFramebufferSize(width: Int, height: Int): Unit = {
    require(width > 0 && height > 0, "Framebuffer dimensions must be positive")
    framebufferWidth = width
    framebufferHeight = height
  }

  // What a window on a desktop can do, and one that is not shown cannot.
  protected def windowCanFullscreen: Boolean = false
  protected def windowIsFullscreen: Boolean = false
  protected def windowSetFullscreen(fullscreen: Boolean): Unit = {}
  private var copied: Option[String] = None
  protected def windowCopyText(text: String): Unit = copied = Some(text)
  protected def windowPasteText(): Option[String] = copied

  class LwjglWindow extends AbstractWindow {
    override def canFullscreen: Boolean = windowCanFullscreen
    override def isFullscreen: Boolean = windowIsFullscreen
    override def setFullscreen(fullscreen: Boolean): Unit = windowSetFullscreen(fullscreen)
    override def copyText(text: String): Unit = windowCopyText(text)
    override def pasteText(): Loader[String] = windowPasteText() match {
      case Some(text) => Loader.successful(text)
      case None => Loader.failed(new NoSuchElementException("There is no text on the clipboard"))
    }

    override def width: Int = framebufferWidth
    override def height: Int = framebufferHeight

    override def xppi: Float = ScreenForcePPI.getOrElse(160f)
    override def yppi: Float = ScreenForcePPI.getOrElse(160f)
    override def logicalPpi: Float = ScreenForcePPI.getOrElse(160f)
  }

  type Window = LwjglWindow
  override val Window: Window = new LwjglWindow
}
