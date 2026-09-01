package sgl.lwjgl

import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.system.MemoryStack

trait LwjglGlfwWindowProvider extends LwjglWindowProvider {
  protected var glfwWindow: Long = 0L

  protected final def setLwjglWindow(window: Long): Unit = {
    glfwWindow = window
    refreshFramebufferSize()
  }

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
