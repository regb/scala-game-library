package sgl.lwjgl

import sgl.{Application, DesktopSystemProvider, Input}
import sgl.util.{LoggingProvider, SingleThreadSchedulerProvider}

import scala.collection.mutable
import java.util.concurrent.ConcurrentLinkedQueue

import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL
import org.lwjgl.system.Platform

trait LwjglApp extends LwjglGlfwWindowProvider with LwjglRenderThreadDispatcher with LwjglOpenGLProvider with LwjglAudioProvider with SingleThreadSchedulerProvider with LwjglFrameCapture {
  this: Application with LoggingProvider with DesktopSystemProvider =>

  val TargetFps: Option[Int] = Some(60)
  val frameDimension: (Int, Int)
  val windowTitle: String = "SGL LWJGL App"

  def viewportWidth: Int = Window.width
  def viewportHeight: Int = Window.height

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

  def main(args: Array[String]): Unit = {
    if(!glfwInit()) throw new RuntimeException("Failed to initialize GLFW")
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    if(Platform.get() == Platform.MACOSX) glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    glfwWindowHint(GLFW_FLOATING, GLFW_TRUE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)

    val window = glfwCreateWindow(frameDimension._1, frameDimension._2, windowTitle, 0L, 0L)
    if(window == 0L) {
      glfwTerminate()
      throw new RuntimeException("Failed to create GLFW window")
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
        }
      }
    })

    glfwSetCursorPosCallback(window, (_, x, y) => {
      mouseX = x.toInt
      mouseY = y.toInt
      Input.inputProcessor.mouseMoved(mouseX, mouseY)
    })

    glfwSetMouseButtonCallback(window, (_, button, action, _) => {
      val mapped = button match {
        case GLFW_MOUSE_BUTTON_LEFT => Some(Input.MouseButtons.Left)
        case GLFW_MOUSE_BUTTON_RIGHT => Some(Input.MouseButtons.Right)
        case GLFW_MOUSE_BUTTON_MIDDLE => Some(Input.MouseButtons.Middle)
        case _ => None
      }
      mapped.foreach { b =>
        if(action == GLFW_PRESS) {
          activeMouseButtons += b
          Input.inputProcessor.mouseDown(mouseX, mouseY, b)
        } else if(action == GLFW_RELEASE && activeMouseButtons.remove(b)) {
          Input.inputProcessor.mouseUp(mouseX, mouseY, b)
        }
      }
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
