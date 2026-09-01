package sgl.lwjgl

import sgl.WindowProvider

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

  class LwjglWindow extends AbstractWindow {
    override def width: Int = framebufferWidth
    override def height: Int = framebufferHeight

    override def xppi: Float = ScreenForcePPI.getOrElse(160f)
    override def yppi: Float = ScreenForcePPI.getOrElse(160f)
    override def logicalPpi: Float = ScreenForcePPI.getOrElse(160f)
  }

  type Window = LwjglWindow
  override val Window: Window = new LwjglWindow
}
