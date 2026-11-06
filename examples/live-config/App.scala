package sgl.examples.liveconfig

import sgl.{Application, CanvasProvider, WindowProvider}

/** Release game code has no dependency on development configuration. */
trait AbstractApp extends Application {
  this: CanvasProvider with WindowProvider =>

  protected def movementSpeed: Double = 100.0
  private var x = 0.0

  override def frame(dt: Double): Unit = {
    x = (x + movementSpeed * dt) % math.max(1, Window.width)
    withFrameCanvas { canvas =>
      canvas.drawRect(0f, 0f, Window.width.toFloat, Window.height.toFloat,
        Graphics.defaultPaint.withColor(Graphics.Color.Black))
      canvas.drawCircle(x.toFloat, Window.height / 2f, 20f,
        Graphics.defaultPaint.withColor(Graphics.Color.Red))
    }
  }
}
