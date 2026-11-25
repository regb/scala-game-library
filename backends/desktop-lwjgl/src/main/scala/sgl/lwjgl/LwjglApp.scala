package sgl.lwjgl

import sgl.{Application, DesktopSystemProvider, Input}
import sgl.util.{LoggingProvider, SingleThreadSchedulerProvider}

import scala.collection.mutable
import java.util.concurrent.ConcurrentLinkedQueue

import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL
import org.lwjgl.system.{MemoryStack, MemoryUtil, Platform}

trait LwjglApp extends LwjglGlfwWindowProvider with LwjglRenderThreadDispatcher with LwjglOpenGLProvider with LwjglAudioProvider with SingleThreadSchedulerProvider with LwjglFrameCapture {
  this: Application with LoggingProvider with DesktopSystemProvider =>

  val TargetFps: Option[Int] = Some(60)
  val frameDimension: (Int, Int)
  val windowTitle: String = "SGL LWJGL App"

  def viewportWidth: Int = Window.width
  def viewportHeight: Int = Window.height

  /** Whether the player can resize the window. The game is told of a new size
    * through `resize`.
    */
  val WindowResizable: Boolean = false

  val WindowInitialPosition: Option[(Int, Int)] = None

  private val renderThreadTasks = new ConcurrentLinkedQueue[() => Unit]
  private val renderTaskLock = new Object
  @volatile private var acceptingRenderTasks = true

  override def runOnOpenGLThread(task: => Unit): Unit = renderTaskLock.synchronized {
    if(!acceptingRenderTasks) throw new IllegalStateException("The OpenGL render thread has stopped")
    renderThreadTasks.add(() => task)
  }

  private def runPendingRenderThreadTasks(): Unit = {
    var task = renderThreadTasks.poll()
    while(task != null) {
      task()
      task = renderThreadTasks.poll()
    }
  }

  /** What GLFW has to say about its last failure, if anything. */
  private def glfwError(): String = {
    val stack = MemoryStack.stackPush()
    try {
      val description = stack.mallocPointer(1)
      val code = glfwGetError(description)
      if(code == GLFW_NO_ERROR || description.get(0) == 0L) ""
      else ": " + MemoryUtil.memUTF8(description.get(0))
    } finally stack.close()
  }

  def main(args: Array[String]): Unit = {
    if(!glfwInit()) throw new RuntimeException("Failed to initialize GLFW" + glfwError())
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    if(Platform.get() == Platform.MACOSX) glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
    glfwWindowHint(GLFW_RESIZABLE, if(WindowResizable) GLFW_TRUE else GLFW_FALSE)
    glfwWindowHint(GLFW_FLOATING, GLFW_TRUE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)

    val window = glfwCreateWindow(frameDimension._1, frameDimension._2, windowTitle, 0L, 0L)
    if(window == 0L) {
      val reason = glfwError()
      glfwTerminate()
      throw new RuntimeException("Failed to create GLFW window" + reason)
    }
    setLwjglWindow(window)
    WindowInitialPosition.foreach { case (x, y) => glfwSetWindowPos(window, x, y) }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    GL.createCapabilities()

    var mouseX = 0
    var mouseY = 0
    val activeMouseButtons = mutable.Set.empty[Input.MouseButtons.MouseButton]
    val pressedKeys = mutable.Set.empty[Input.Keys.Key]

    def releaseInputs(): Unit = {
      activeMouseButtons.foreach(button => Input.inputProcessor.mouseUp(mouseX, mouseY, button))
      activeMouseButtons.clear()
      pressedKeys.foreach(Input.inputProcessor.keyUp)
      pressedKeys.clear()
    }

    glfwSetKeyCallback(window, (_, key, _, action, _) => {
      val mapped = key match {
        case GLFW_KEY_LEFT => Some(Input.Keys.Left)
        case GLFW_KEY_RIGHT => Some(Input.Keys.Right)
        case GLFW_KEY_UP => Some(Input.Keys.Up)
        case GLFW_KEY_DOWN => Some(Input.Keys.Down)
        case GLFW_KEY_A => Some(Input.Keys.A)
        case GLFW_KEY_D => Some(Input.Keys.D)
        case GLFW_KEY_W => Some(Input.Keys.W)
        case GLFW_KEY_S => Some(Input.Keys.S)
        case GLFW_KEY_SPACE => Some(Input.Keys.Space)
        case GLFW_KEY_ENTER => Some(Input.Keys.Enter)
        case GLFW_KEY_KP_ENTER => Some(Input.Keys.NumpadEnter)
        case GLFW_KEY_ESCAPE => Some(Input.Keys.Escape)
        case GLFW_KEY_BACKSPACE => Some(Input.Keys.Backspace)
        case GLFW_KEY_LEFT_SHIFT => Some(Input.Keys.LeftShift)
        case GLFW_KEY_RIGHT_SHIFT => Some(Input.Keys.RightShift)
        case GLFW_KEY_LEFT_CONTROL => Some(Input.Keys.LeftControl)
        case GLFW_KEY_RIGHT_CONTROL => Some(Input.Keys.RightControl)
        case GLFW_KEY_LEFT_ALT => Some(Input.Keys.LeftAlt)
        case GLFW_KEY_RIGHT_ALT => Some(Input.Keys.RightAlt)
        case GLFW_KEY_TAB => Some(Input.Keys.Tab)
        case GLFW_KEY_DELETE => Some(Input.Keys.Delete)
        case GLFW_KEY_HOME => Some(Input.Keys.Home)
        case GLFW_KEY_END => Some(Input.Keys.End)
        case GLFW_KEY_PAGE_UP => Some(Input.Keys.PageUp)
        case GLFW_KEY_PAGE_DOWN => Some(Input.Keys.PageDown)
        case GLFW_KEY_F1 => Some(Input.Keys.F1)
        case GLFW_KEY_F2 => Some(Input.Keys.F2)
        case GLFW_KEY_F3 => Some(Input.Keys.F3)
        case GLFW_KEY_F4 => Some(Input.Keys.F4)
        case GLFW_KEY_F5 => Some(Input.Keys.F5)
        case GLFW_KEY_F6 => Some(Input.Keys.F6)
        case GLFW_KEY_F7 => Some(Input.Keys.F7)
        case GLFW_KEY_F8 => Some(Input.Keys.F8)
        case GLFW_KEY_F9 => Some(Input.Keys.F9)
        case GLFW_KEY_F10 => Some(Input.Keys.F10)
        case GLFW_KEY_F11 => Some(Input.Keys.F11)
        case GLFW_KEY_F12 => Some(Input.Keys.F12)
        case GLFW_KEY_B => Some(Input.Keys.B)
        case GLFW_KEY_C => Some(Input.Keys.C)
        case GLFW_KEY_E => Some(Input.Keys.E)
        case GLFW_KEY_F => Some(Input.Keys.F)
        case GLFW_KEY_G => Some(Input.Keys.G)
        case GLFW_KEY_H => Some(Input.Keys.H)
        case GLFW_KEY_I => Some(Input.Keys.I)
        case GLFW_KEY_J => Some(Input.Keys.J)
        case GLFW_KEY_K => Some(Input.Keys.K)
        case GLFW_KEY_L => Some(Input.Keys.L)
        case GLFW_KEY_M => Some(Input.Keys.M)
        case GLFW_KEY_N => Some(Input.Keys.N)
        case GLFW_KEY_O => Some(Input.Keys.O)
        case GLFW_KEY_P => Some(Input.Keys.P)
        case GLFW_KEY_Q => Some(Input.Keys.Q)
        case GLFW_KEY_R => Some(Input.Keys.R)
        case GLFW_KEY_T => Some(Input.Keys.T)
        case GLFW_KEY_U => Some(Input.Keys.U)
        case GLFW_KEY_V => Some(Input.Keys.V)
        case GLFW_KEY_X => Some(Input.Keys.X)
        case GLFW_KEY_Y => Some(Input.Keys.Y)
        case GLFW_KEY_Z => Some(Input.Keys.Z)
        case GLFW_KEY_0 => Some(Input.Keys.Num0)
        case GLFW_KEY_1 => Some(Input.Keys.Num1)
        case GLFW_KEY_2 => Some(Input.Keys.Num2)
        case GLFW_KEY_3 => Some(Input.Keys.Num3)
        case GLFW_KEY_4 => Some(Input.Keys.Num4)
        case GLFW_KEY_5 => Some(Input.Keys.Num5)
        case GLFW_KEY_6 => Some(Input.Keys.Num6)
        case GLFW_KEY_7 => Some(Input.Keys.Num7)
        case GLFW_KEY_8 => Some(Input.Keys.Num8)
        case GLFW_KEY_9 => Some(Input.Keys.Num9)
        case _ => None
      }
      mapped.foreach { k =>
        if(action == GLFW_PRESS) {
          pressedKeys += k
          Input.inputProcessor.keyDown(k)
        } else if(action == GLFW_RELEASE) {
          pressedKeys -= k
          Input.inputProcessor.keyUp(k)
        } else if(action == GLFW_REPEAT && pressedKeys.contains(k)) {
          Input.inputProcessor.keyRepeated(k)
        }
      }
    })

    glfwSetCharCallback(window, (_, codePoint) => {
      if(!Character.isISOControl(codePoint)) Input.inputProcessor.textInput(new String(Character.toChars(codePoint)))
    })

    glfwSetCursorPosCallback(window, (_, x, y) => {
      mouseX = x.toInt
      mouseY = y.toInt
      Input.inputProcessor.mouseMoved(mouseX, mouseY)
    })

    glfwSetMouseButtonCallback(window, (_, button, action, _) => {
      val b = button match {
        case GLFW_MOUSE_BUTTON_LEFT => Input.MouseButtons.Left
        case GLFW_MOUSE_BUTTON_RIGHT => Input.MouseButtons.Right
        case GLFW_MOUSE_BUTTON_MIDDLE => Input.MouseButtons.Middle
        case other => Input.MouseButtons.Other(other)
      }
      if(action == GLFW_PRESS) {
        activeMouseButtons += b
        Input.inputProcessor.mouseDown(mouseX, mouseY, b)
      } else if(action == GLFW_RELEASE && activeMouseButtons.remove(b)) {
        Input.inputProcessor.mouseUp(mouseX, mouseY, b)
      }
    })

    // GLFW counts up and to the left.
    glfwSetScrollCallback(window, (_, x, y) => {
      Input.inputProcessor.mouseScrolled(0f - x.toFloat, 0f - y.toFloat, Input.ScrollUnits.Notches)
    })

    glfwSetCursorEnterCallback(window, (_, entered) => {
      if(!entered) releaseInputs()
    })

    glfwSetWindowFocusCallback(window, (_, focused) => {
      if(!focused) {
        releaseInputs()
        pause()
      } else resume()
    })

    glfwSetFramebufferSizeCallback(window, (_, width, height) => {
      refreshFramebufferSize()
      resize(width, height)
    })

    var applicationCreated = false
    try {
      applicationCreated = true
      create()
      resize(Window.width, Window.height)

      val targetFramePeriod = TargetFps.map(Application.framePeriodMillis)
      var last = java.lang.System.nanoTime()
      while(!glfwWindowShouldClose(window)) {
        val begin = java.lang.System.nanoTime()
        val now = java.lang.System.nanoTime()
        val dt = (now - last).toDouble / 1000000000.0
        last = now

        glfwPollEvents()
        runPendingRenderThreadTasks()
        Audio.update()
        val captureBatch = beginFrameCapture()
        try {
          frame(dt)
          completeFrameCapture(captureBatch, captureOpenGLFrame())
        } catch {
          case error: Throwable =>
            failFrameCapture(captureBatch, error)
            throw error
        }
        glfwSwapBuffers(window)

        val current = java.lang.System.nanoTime()
        Scheduler.run(targetFramePeriod.map(fp => fp - (current - begin) / 1000000L).getOrElse(10L))
      }
    } finally {
      runPendingRenderThreadTasks()
      releaseInputs()
      try {
        if(applicationCreated) dispose()
      } finally {
        renderTaskLock.synchronized { acceptingRenderTasks = false }
        runPendingRenderThreadTasks()
        try disposeAudio()
        finally {
          glfwDestroyWindow(window)
          glfwTerminate()
        }
      }
    }
  }

}
