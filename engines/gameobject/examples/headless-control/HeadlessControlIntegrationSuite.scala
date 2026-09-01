package sgl.examples.gameobject.headlesscontrol

import org.scalatest.funsuite.AnyFunSuite
import sgl.{Application, CanvasProvider, CapturedFrame, DesktopSystemProvider, Input, OpenGLCanvasProvider, OpenGLProvider, WindowProvider}
import sgl.engine.gameobject._
import sgl.engine.gameobject.capability._
import sgl.engine.gameobject.render2d._
import sgl.engine.gameobject.runtime._
import sgl.engine.gameobject.spatial2d._
import sgl.headless.opengl.HeadlessOpenGLApp
import sgl.math.Vec2
import sgl.util.NoLoggingProvider

class HeadlessControlIntegrationSuite extends AnyFunSuite {
  test("Canvas draws a programmatically created RGBA texture") {
    object app extends HeadlessOpenGLApp
        with OpenGLCanvasProvider
        with HardCodedBitmapGame
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (32, 32)
    }

    app.start()
    try {
      val frame = captureNextStep(app)
      assertPixel(frame, 10, 10, red = 255, green = 0, blue = 0, alpha = 255)
      assertPixel(frame, 20, 10, red = 0, green = 255, blue = 0, alpha = 255)
      assertPixel(frame, 10, 20, red = 0, green = 0, blue = 255, alpha = 255)
      assertPixel(frame, 20, 20, red = 255, green = 255, blue = 255, alpha = 255)
    } finally app.stop()
  }

  test("one Right input moves a Canvas rectangle exactly 16 pixels") {
    object app extends HeadlessOpenGLApp
        with OpenGLCanvasProvider
        with HardCodedCanvasGame
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (64, 64)
    }

    app.start()
    try {
      val before = captureNextStep(app)
      app.Controller.keyDown(Input.Keys.Right)
      val after = captureNextStep(app)
      app.Controller.keyUp(Input.Keys.Right)

      assertPixel(before, 10, 30, red = 0, green = 255, blue = 255, alpha = 255)
      assertPixel(before, 26, 30, red = 0, green = 0, blue = 0, alpha = 255)
      assertPixel(after, 10, 30, red = 0, green = 0, blue = 0, alpha = 255)
      assertPixel(after, 26, 30, red = 0, green = 255, blue = 255, alpha = 255)
      assertPixel(after, 42, 10, red = 255, green = 0, blue = 255, alpha = 255)
      assertPixel(after, 46, 10, red = 0, green = 0, blue = 0, alpha = 255)
      assertRegionContainsWhite(after, left = 40, top = 42, right = 48, bottom = 51)
    } finally app.stop()
  }

  test("one Right input moves the triangle exactly 16 pixels") {
    object app extends HeadlessOpenGLApp
        with HardCodedMovementGame
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (64, 64)
    }

    app.start()
    try {
      val before = captureNextStep(app)

      app.Controller.keyDown(Input.Keys.Right)
      val after = captureNextStep(app)
      app.Controller.keyUp(Input.Keys.Right)

      val beforeCenter = coloredPixelCenter(before)
      val afterCenter = coloredPixelCenter(after)
      assert(math.abs(afterCenter._1 - beforeCenter._1 - 16.0) < 0.01)
      assert(math.abs(afterCenter._2 - beforeCenter._2) < 0.01)
    } finally app.stop()
  }

  private def captureNextStep(app: HeadlessOpenGLApp): CapturedFrame = {
    val capture = app.captureNextFrame()
    app.step(1.0 / 60.0)
    capture.value.flatMap(_.toOption).getOrElse(
      throw new IllegalStateException("Headless frame capture did not complete")
    )
  }

  private def assertPixel(frame: CapturedFrame, x: Int, y: Int, red: Int, green: Int, blue: Int, alpha: Int): Unit = {
    val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
    assert((frame.rgba(offset) & 0xff) == red)
    assert((frame.rgba(offset + 1) & 0xff) == green)
    assert((frame.rgba(offset + 2) & 0xff) == blue)
    assert((frame.rgba(offset + 3) & 0xff) == alpha)
  }

  private def assertRegionContainsWhite(frame: CapturedFrame, left: Int, top: Int, right: Int, bottom: Int): Unit = {
    val containsWhite = (top until bottom).exists { y =>
      (left until right).exists { x =>
        val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
        (frame.rgba(offset) & 0xff) > 128 &&
        (frame.rgba(offset + 1) & 0xff) > 128 &&
        (frame.rgba(offset + 2) & 0xff) > 128
      }
    }
    assert(containsWhite)
  }

  private def coloredPixelCenter(frame: CapturedFrame): (Double, Double) = {
    var xTotal = 0L
    var yTotal = 0L
    var count = 0L
    var y = 0
    while(y < frame.height) {
      var x = 0
      while(x < frame.width) {
        val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
        val red = frame.rgba(offset) & 0xff
        val green = frame.rgba(offset + 1) & 0xff
        val blue = frame.rgba(offset + 2) & 0xff
        if(red < 100 && green > 180 && blue > 220) {
          xTotal += x
          yTotal += y
          count += 1
        }
        x += 1
      }
      y += 1
    }
    assert(count > 0, "Expected the rendered triangle to contain cyan pixels")
    (xTotal.toDouble / count, yTotal.toDouble / count)
  }
}

trait HardCodedBitmapGame extends Application {
  this: OpenGLCanvasProvider with OpenGLProvider with WindowProvider =>

  private var bitmap: Graphics.Bitmap = _

  override def create(): Unit = {
    val pixels = Array[Byte](
      -1, 0, 0, -1, 0, -1, 0, -1,
      0, 0, -1, -1, -1, -1, -1, -1
    )
    bitmap = new Graphics.OpenGLBitmap(OpenGL.createTextureImage2D(2, 2, pixels))
  }

  override def frame(dt: Double): Unit = withFrameCanvas { canvas =>
    canvas.drawRect(0f, 0f, Window.width.toFloat, Window.height.toFloat,
      Graphics.defaultPaint.withColor(Graphics.Color.Black))
    canvas.drawBitmap(bitmap, 8f, 8f, 16f, 16f, 0, 0, 2, 2, 1f)
  }

  override def dispose(): Unit = {
    if(bitmap != null) bitmap.release()
    super.dispose()
  }
}

trait HardCodedCanvasGame extends Application {
  this: CanvasProvider with WindowProvider =>

  private var rectangleX = 8f

  override def frame(dt: Double): Unit = {
    if(Input.isKeyPressed(Input.Keys.Right)) rectangleX += 16f
    withFrameCanvas { canvas =>
      canvas.drawRect(0f, 0f, Window.width.toFloat, Window.height.toFloat,
        Graphics.defaultPaint.withColor(Graphics.Color.Black))
      canvas.drawRect(rectangleX, 28f, 8f, 8f,
        Graphics.defaultPaint.withColor(Graphics.Color.Cyan))
      canvas.withSave {
        canvas.translate(40f, 8f)
        canvas.clipRect(0f, 0f, 4f, 8f)
        canvas.drawRect(0f, 0f, 8f, 8f,
          Graphics.defaultPaint.withColor(Graphics.Color.Magenta))
      }
      canvas.drawString("A", 40f, 50f,
        Graphics.defaultPaint
          .withColor(Graphics.Color.White)
          .withFont(Graphics.Font.Monospace.withSize(7)))
    }
  }
}

trait HardCodedMovementGame extends OpenGLGameObjectApp {
  override def configure(builder: GameBuilder): Unit = {
    builder
      .install(UpdateCapabilitiesModule)
      .install(Spatial2DModule)
      .setContext(ClearColor(0f, 0f, 0f, 1f))
      .setContext(FixedDeltaTime(1.0 / 60.0))
      .setInitialScene(HardCodedMovementScene)
  }
}

final class MoveRight(override val owner: GameObject) extends Component with Updatable {
  override def update(context: GameContext)(using Scope): Unit = {
    if(Input.isKeyPressed(Input.Keys.Right)) {
      val spatial = owner.require[Spatial2D]
      spatial.local = spatial.local.copy(position = spatial.local.position + Vec2(16f, 0f))
    }
  }
}
object MoveRight {
  given ComponentKey[MoveRight] = ComponentKey[MoveRight]("sgl.examples.gameobject.headlesscontrol.MoveRight")
}

object HardCodedMovementScene extends Scene {
  override def load(context: GameContext)(using Scope): Unit = {
    context.gameObject2D("Camera", Transform2D()) { camera =>
      camera.attach(new Camera2D(_, Vec2(64f, 64f)))
    }
    context.gameObject2D("Triangle", Transform2D(position = Vec2(-16f, 0f))) { triangle =>
      triangle.attach(new MoveRight(_))
      triangle.attach(new SpriteRenderer(_, SpriteVisual.Triangle((0.2f, 0.8f, 1f, 1f)), 10f, 10f))
    }
  }
}
