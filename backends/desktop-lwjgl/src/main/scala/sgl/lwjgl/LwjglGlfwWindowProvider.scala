package sgl.lwjgl

import org.lwjgl.glfw.GLFW._
import org.lwjgl.system.MemoryStack

trait LwjglGlfwWindowProvider extends LwjglWindowProvider {
  protected var glfwWindow: Long = 0L

  protected final def setLwjglWindow(window: Long): Unit = {
    glfwWindow = window
    refreshFramebufferSize()
  }

  override protected def windowCanFullscreen: Boolean = glfwWindow != 0L && glfwGetPrimaryMonitor() != 0L
  override protected def windowIsFullscreen: Boolean = glfwWindow != 0L && glfwGetWindowMonitor(glfwWindow) != 0L

  // Where the window was, to go back to.
  private var windowed = (0, 0, 800, 600)
  override protected def windowSetFullscreen(fullscreen: Boolean): Unit = if(windowCanFullscreen && fullscreen != windowIsFullscreen) {
    if(fullscreen) {
      val stack = MemoryStack.stackPush()
      try {
        val (x, y, width, height) = (stack.mallocInt(1), stack.mallocInt(1), stack.mallocInt(1), stack.mallocInt(1))
        glfwGetWindowPos(glfwWindow, x, y)
        glfwGetWindowSize(glfwWindow, width, height)
        windowed = (x.get(0), y.get(0), width.get(0), height.get(0))
      } finally stack.pop()
      val monitor = glfwGetPrimaryMonitor()
      val mode = glfwGetVideoMode(monitor)
      if(mode != null) glfwSetWindowMonitor(glfwWindow, monitor, 0, 0, mode.width, mode.height, mode.refreshRate)
    } else {
      val (x, y, width, height) = windowed
      glfwSetWindowMonitor(glfwWindow, 0L, x, y, width, height, GLFW_DONT_CARE)
    }
  }

  override protected def windowCopyText(text: String): Unit = if(glfwWindow != 0L) glfwSetClipboardString(glfwWindow, text)
  override protected def windowPasteText(): Option[String] = if(glfwWindow == 0L) None else Option(glfwGetClipboardString(glfwWindow))

  protected final def refreshFramebufferSize(): Unit = {
    if(glfwWindow != 0L) {
      val stack = MemoryStack.stackPush()
      try {
        val width = stack.mallocInt(1)
        val height = stack.mallocInt(1)
        glfwGetFramebufferSize(glfwWindow, width, height)
        setFramebufferSize(width.get(0), height.get(0))
      } finally stack.pop()
    }
  }
}
