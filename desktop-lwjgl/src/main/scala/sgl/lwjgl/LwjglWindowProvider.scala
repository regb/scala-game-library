package sgl
package lwjgl

import org.lwjgl.glfw.GLFW._
import org.lwjgl.system.MemoryStack

trait LwjglWindowProvider extends WindowProvider {

  val frameDimension: (Int, Int)
  val ScreenForcePPI: Option[Float] = None

  protected var glfwWindow: Long = 0L
  private var framebufferWidth: Int = 0
  private var framebufferHeight: Int = 0

  protected def setLwjglWindow(window: Long): Unit = {
    glfwWindow = window
    refreshFramebufferSize()
  }

  protected def refreshFramebufferSize(): Unit = {
    if(glfwWindow != 0L) {
      val stack = MemoryStack.stackPush()
      try {
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        glfwGetFramebufferSize(glfwWindow, w, h)
        framebufferWidth = w.get(0)
        framebufferHeight = h.get(0)
      } finally stack.pop()
    }
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
