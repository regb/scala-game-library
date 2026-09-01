package sgl.headless.opengl

import org.lwjgl.BufferUtils
import org.lwjgl.egl.EGL
import org.lwjgl.egl.EGL10._
import org.lwjgl.egl.EGL12.{EGL_RENDERABLE_TYPE, eglBindAPI}
import org.lwjgl.egl.EGL14.{EGL_DEFAULT_DISPLAY, EGL_OPENGL_API, EGL_OPENGL_BIT}
import org.lwjgl.egl.KHRCreateContext._
import org.lwjgl.opengl.{GL, GL11C}
import sgl.{Application, DesktopSystemProvider, FakeAudioProvider, Input, InputActions}
import sgl.lwjgl.{LwjglFrameCapture, LwjglOpenGLProvider, LwjglRenderThreadDispatcher, LwjglWindowProvider}
import sgl.util.{LoggingProvider, SingleThreadSchedulerProvider}

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable

/** Manually driven OpenGL application backed by a surfaceless EGL pbuffer.
  *
  * Mesa can run this backend entirely in software with
  * `LIBGL_ALWAYS_SOFTWARE=1`, without X11, Wayland, or a physical GPU. Input
  * methods enqueue events from any thread. `step` dispatches them on the EGL
  * context thread before updating the game.
  */
trait HeadlessOpenGLApp extends LwjglWindowProvider
    with LwjglRenderThreadDispatcher
    with LwjglOpenGLProvider
    with FakeAudioProvider
    with SingleThreadSchedulerProvider
    with LwjglFrameCapture {
  this: Application with LoggingProvider with DesktopSystemProvider =>

  val frameDimension: (Int, Int)

  private val renderThreadTasks = new ConcurrentLinkedQueue[() => Unit]
  private val renderTaskLock = new Object
  private val inputEvents = new ConcurrentLinkedQueue[() => Unit]
  private val inputLock = new Object
  private val pressedKeys = mutable.LinkedHashSet.empty[Input.Keys.Key]
  private val pressedMouseButtons = mutable.LinkedHashSet.empty[Input.MouseButtons.MouseButton]
  private val activeTouches = mutable.Map.empty[Int, (Int, Int)]

  @volatile private var acceptingRenderTasks = false
  private var acceptingInput = false
  private var ownerThread: Thread = _
  private var mouseX = 0
  private var mouseY = 0
  private var display = EGL_NO_DISPLAY
  private var surface = EGL_NO_SURFACE
  private var context = EGL_NO_CONTEXT
  @volatile private var started = false

  /** Input endpoint for tests, agents, and interactive controller processes. */
  object Controller {
    def keyDown(key: Input.Keys.Key): Unit = enqueueInput {
      if(pressedKeys.add(key)) Input.inputProcessor.keyDown(key)
    }

    def keyUp(key: Input.Keys.Key): Unit = enqueueInput {
      if(pressedKeys.remove(key)) Input.inputProcessor.keyUp(key)
    }

    def mouseMoved(x: Int, y: Int): Unit = enqueueInput {
      mouseX = x
      mouseY = y
      Input.inputProcessor.mouseMoved(x, y)
    }

    def mouseDown(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Unit = enqueueInput {
      mouseX = x
      mouseY = y
      if(pressedMouseButtons.add(button)) Input.inputProcessor.mouseDown(x, y, button)
    }

    def mouseUp(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Unit = enqueueInput {
      mouseX = x
      mouseY = y
      if(pressedMouseButtons.remove(button)) Input.inputProcessor.mouseUp(x, y, button)
    }

    def mouseScrolled(amount: Int): Unit = enqueueInput {
      Input.inputProcessor.mouseScrolled(amount)
    }

    def touchDown(x: Int, y: Int, pointer: Int): Unit = {
      requireValidPointer(pointer)
      enqueueInput {
        if(!activeTouches.contains(pointer)) {
          activeTouches(pointer) = (x, y)
          Input.inputProcessor.touchDown(x, y, pointer)
        }
      }
    }

    def touchMoved(x: Int, y: Int, pointer: Int): Unit = {
      requireValidPointer(pointer)
      enqueueInput {
        if(activeTouches.contains(pointer)) {
          activeTouches(pointer) = (x, y)
          Input.inputProcessor.touchMoved(x, y, pointer)
        }
      }
    }

    def touchUp(x: Int, y: Int, pointer: Int): Unit = {
      requireValidPointer(pointer)
      enqueueInput {
        if(activeTouches.remove(pointer).isDefined) Input.inputProcessor.touchUp(x, y, pointer)
      }
    }

    def systemAction(action: InputActions.Action): Unit = enqueueInput {
      Input.inputProcessor.systemAction(action)
    }

    def releaseAll(): Unit = enqueueInput(releaseInputs())
  }

  final def rendererName: String = {
    requireStarted()
    Option(GL11C.glGetString(GL11C.GL_RENDERER)).getOrElse("unknown")
  }

  final def start(): Unit = {
    if(started) throw new IllegalStateException("Headless application is already started")
    ownerThread = Thread.currentThread()
    initializeEgl()
    setFramebufferSize(frameDimension._1, frameDimension._2)
    acceptingRenderTasks = true
    inputLock.synchronized { acceptingInput = true }
    started = true
    try {
      create()
      resize(Window.width, Window.height)
    } catch {
      case error: Throwable =>
        inputLock.synchronized { acceptingInput = false }
        try dispose()
        catch { case cleanupError: Throwable => error.addSuppressed(cleanupError) }
        renderTaskLock.synchronized { acceptingRenderTasks = false }
        try runPendingRenderThreadTasks()
        catch { case cleanupError: Throwable => error.addSuppressed(cleanupError) }
        started = false
        destroyEgl()
        throw error
    }
  }

  final def step(dt: Double): Unit = {
    requireStarted()
    requireOwnerThread()
    require(dt >= 0.0, "Frame delta must not be negative")
    runPendingRenderThreadTasks()
    runPendingInputEvents()
    val captureBatch = beginFrameCapture()
    try {
      frame(dt)
      completeFrameCapture(captureBatch, captureOpenGLFrame())
    } catch {
      case error: Throwable =>
        failFrameCapture(captureBatch, error)
        throw error
    }
    Scheduler.run(10L)
  }

  final def stop(): Unit = {
    if(!started) return
    requireOwnerThread()
    inputLock.synchronized { acceptingInput = false }
    runPendingRenderThreadTasks()
    runPendingInputEvents()
    releaseInputs()
    try dispose()
    finally {
      renderTaskLock.synchronized { acceptingRenderTasks = false }
      runPendingRenderThreadTasks()
      started = false
      destroyEgl()
    }
  }

  override def runOnOpenGLThread(task: => Unit): Unit = renderTaskLock.synchronized {
    if(!acceptingRenderTasks) throw new IllegalStateException("The headless OpenGL context is not running")
    if(Thread.currentThread() eq ownerThread) task
    else renderThreadTasks.add(() => task)
  }

  private def enqueueInput(event: => Unit): Unit = inputLock.synchronized {
    if(!acceptingInput) throw new IllegalStateException("The headless input controller is not running")
    inputEvents.add(() => event)
  }

  private def runPendingInputEvents(): Unit = {
    var event = inputEvents.poll()
    while(event != null) {
      event()
      event = inputEvents.poll()
    }
  }

  private def releaseInputs(): Unit = {
    pressedMouseButtons.foreach(button => Input.inputProcessor.mouseUp(mouseX, mouseY, button))
    pressedMouseButtons.clear()
    pressedKeys.foreach(Input.inputProcessor.keyUp)
    pressedKeys.clear()
    activeTouches.toSeq.sortBy(_._1).foreach { case (pointer, (x, y)) =>
      Input.inputProcessor.touchUp(x, y, pointer)
    }
    activeTouches.clear()
  }

  private def requireValidPointer(pointer: Int): Unit =
    require(pointer >= 0 && pointer < 10, "Touch pointer must be between 0 and 9")

  private def runPendingRenderThreadTasks(): Unit = {
    var task = renderThreadTasks.poll()
    while(task != null) {
      task()
      task = renderThreadTasks.poll()
    }
  }

  private def initializeEgl(): Unit = {
    try EGL.getFunctionProvider()
    catch { case _: IllegalStateException => EGL.create() }
    try {
      display = eglGetDisplay(EGL_DEFAULT_DISPLAY)
      checkHandle(display, EGL_NO_DISPLAY, "get the EGL display; Mesa requires EGL_PLATFORM=surfaceless")

      val major = BufferUtils.createIntBuffer(1)
      val minor = BufferUtils.createIntBuffer(1)
      checkEgl(eglInitialize(display, major, minor), "initialize EGL")
      val _ = EGL.createDisplayCapabilities(display, major.get(0), minor.get(0))
      checkEgl(eglBindAPI(EGL_OPENGL_API), "bind the desktop OpenGL API")

      val configs = BufferUtils.createPointerBuffer(1)
      val configCount = new Array[Int](1)
      val configAttributes = Array(
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_DEPTH_SIZE, 24,
        EGL_STENCIL_SIZE, 8,
        EGL_NONE
      )
      checkEgl(eglChooseConfig(display, configAttributes, configs, configCount), "choose an EGL config")
      if(configCount(0) == 0) throw new IllegalStateException("No EGL pbuffer config supports desktop OpenGL")
      val config = configs.get(0)

      surface = eglCreatePbufferSurface(display, config, Array(
        EGL_WIDTH, frameDimension._1,
        EGL_HEIGHT, frameDimension._2,
        EGL_NONE
      ))
      checkHandle(surface, EGL_NO_SURFACE, "create an EGL pbuffer")

      context = eglCreateContext(display, config, EGL_NO_CONTEXT, Array(
        EGL_CONTEXT_MAJOR_VERSION_KHR, 3,
        EGL_CONTEXT_MINOR_VERSION_KHR, 3,
        EGL_CONTEXT_OPENGL_PROFILE_MASK_KHR, EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT_KHR,
        EGL_NONE
      ))
      checkHandle(context, EGL_NO_CONTEXT, "create an OpenGL 3.3 core context")
      checkEgl(eglMakeCurrent(display, surface, surface, context), "make the EGL context current")
      GL.createCapabilities()
    } catch {
      case error: Throwable =>
        destroyEgl()
        throw error
    }
  }

  private def destroyEgl(): Unit = {
    if(display != EGL_NO_DISPLAY) {
      val _ = eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
      if(context != EGL_NO_CONTEXT) {
        val _ = eglDestroyContext(display, context)
      }
      if(surface != EGL_NO_SURFACE) {
        val _ = eglDestroySurface(display, surface)
      }
      val _ = eglTerminate(display)
    }
    context = EGL_NO_CONTEXT
    surface = EGL_NO_SURFACE
    display = EGL_NO_DISPLAY
    GL.setCapabilities(null)
  }

  private def checkEgl(success: Boolean, operation: String): Unit =
    if(!success) throw new IllegalStateException(s"Could not $operation: EGL error 0x${Integer.toHexString(eglGetError())}")

  private def checkHandle(handle: Long, invalid: Long, operation: String): Unit =
    if(handle == invalid) throw new IllegalStateException(s"Could not $operation: EGL error 0x${Integer.toHexString(eglGetError())}")

  private def requireStarted(): Unit =
    if(!started) throw new IllegalStateException("Headless application is not started")

  private def requireOwnerThread(): Unit =
    if(Thread.currentThread() ne ownerThread)
      throw new IllegalStateException("The headless application must be driven from its EGL context thread")
}
