package sgl.headless.opengl

import org.scalatest.funsuite.AnyFunSuite
import sgl.{Application, CapturedFrame, DesktopSystemProvider, Input, InputProcessor}
import sgl.util.NoLoggingProvider

class HeadlessOpenGLAppSuite extends AnyFunSuite {
  test("surfaceless EGL accepts input and captures frames with llvmpipe") {
    var keyDownEvents = 0
    var keyUpEvents = 0

    object app extends HeadlessOpenGLApp
        with Application
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (8, 6)

      override def create(): Unit = Input.setInputProcessor(new InputProcessor {
        override def keyDown(key: Input.Keys.Key): Boolean = {
          keyDownEvents += 1
          false
        }

        override def keyUp(key: Input.Keys.Key): Boolean = {
          keyUpEvents += 1
          false
        }
      })

      override def frame(dt: Double): Unit = {
        OpenGL.viewport(0, 0, Window.width, Window.height)
        if(Input.isKeyPressed(Input.Keys.Right)) OpenGL.clearColor(0.25f, 0.5f, 0.75f, 1.0f)
        else OpenGL.clearColor(0.75f, 0.5f, 0.25f, 1.0f)
        OpenGL.clear(OpenGL.ColorBufferBit)
      }

      override def dispose(): Unit = Input.clearInputProcessor()
    }

    app.start()
    try {
      assert(app.rendererName.toLowerCase.contains("llvmpipe"), s"Expected llvmpipe, got ${app.rendererName}")

      val pressedFrame = app.captureNextFrame()
      val controllerThread = new Thread(() => app.Controller.keyDown(Input.Keys.Right))
      controllerThread.start()
      controllerThread.join()
      app.step(1.0 / 60.0)

      assert(keyDownEvents == 1)
      assertEveryPixel(pressedFrame.value.get.get, red = 64, green = 128, blue = 191, alpha = 255)

      val releasedFrame = app.captureNextFrame()
      app.Controller.keyUp(Input.Keys.Right)
      app.step(1.0 / 60.0)

      assert(keyUpEvents == 1)
      assertEveryPixel(releasedFrame.value.get.get, red = 191, green = 128, blue = 64, alpha = 255)
    } finally app.stop()
  }

  private def assertEveryPixel(frame: CapturedFrame, red: Int, green: Int, blue: Int, alpha: Int): Unit = {
    var offset = 0
    while(offset < frame.rgba.length) {
      assert((frame.rgba(offset) & 0xff) == red)
      assert((frame.rgba(offset + 1) & 0xff) == green)
      assert((frame.rgba(offset + 2) & 0xff) == blue)
      assert((frame.rgba(offset + 3) & 0xff) == alpha)
      offset += CapturedFrame.BytesPerPixel
    }
  }
}
