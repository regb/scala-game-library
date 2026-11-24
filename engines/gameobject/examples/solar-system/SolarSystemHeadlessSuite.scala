package sgl.examples.gameobject.solar

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import org.scalatest.funsuite.AnyFunSuite
import sgl.{CapturedFrame, DesktopSystemProvider}
import sgl.headless.opengl.HeadlessOpenGLApp
import sgl.util.NoLoggingProvider

/** Runs the real game without a window and looks at what it draws. */
class SolarSystemHeadlessSuite extends AnyFunSuite {

  test("the scene appears with the sun lit in the middle") {
    object app extends HeadlessOpenGLApp
        with core.AbstractApp
        with core.GeneratedAssetsProvider
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (400, 280)
    }

    app.start()
    try {
      val deadline = java.lang.System.nanoTime() + 20_000_000_000L
      var frame = capture(app)
      while(litPixels(frame) == 0 && java.lang.System.nanoTime() < deadline) {
        Thread.sleep(20)
        frame = capture(app)
      }
      // A few more frames so that the bodies have moved from their start.
      (0 until 30).foreach(_ => app.step(1.0 / 60.0))
      frame = capture(app)
      save(frame, "loaded")

      // The sun is emissive and sits at the origin, which the camera looks at.
      val (red, green, blue) = pixel(frame, frame.width / 2, frame.height / 2)
      assert(red > 150 && green > 90, s"expected the sun in the middle, got ($red, $green, $blue)")
      assert(red > blue, s"expected a warm sun, got ($red, $green, $blue)")
      // Space around it stays dark.
      val (cornerRed, cornerGreen, cornerBlue) = pixel(frame, 4, 4)
      assert(cornerRed < 20 && cornerGreen < 20 && cornerBlue < 30)
      // The sun, the earth and the moon cover a part of the view, not all of it.
      val lit = litPixels(frame)
      assert(lit > frame.width * frame.height / 100 && lit < frame.width * frame.height / 2)

      // Later on, the moon has come out from behind the earth.
      (0 until 45).foreach(_ => app.step(1.0 / 60.0))
      save(capture(app), "later")
    } finally app.stop()
  }

  private def capture(app: HeadlessOpenGLApp): CapturedFrame = {
    val pending = app.captureNextFrame()
    app.step(1.0 / 60.0)
    pending.value.flatMap(_.toOption).getOrElse(
      throw new IllegalStateException("Headless frame capture did not complete")
    )
  }

  private def pixel(frame: CapturedFrame, x: Int, y: Int): (Int, Int, Int) = {
    val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
    (frame.rgba(offset) & 0xff, frame.rgba(offset + 1) & 0xff, frame.rgba(offset + 2) & 0xff)
  }

  private def litPixels(frame: CapturedFrame): Int = {
    var count = 0
    var y = 0
    while(y < frame.height) {
      var x = 0
      while(x < frame.width) {
        val (red, green, blue) = pixel(frame, x, y)
        if(red + green + blue > 90) count += 1
        x += 1
      }
      y += 1
    }
    count
  }

  // Kept with the test outputs, to look at when the test fails.
  private def save(frame: CapturedFrame, name: String): Unit = {
    Option(java.lang.System.getenv("TEST_UNDECLARED_OUTPUTS_DIR")).foreach { directory =>
      val image = new BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_RGB)
      for(y <- 0 until frame.height; x <- 0 until frame.width) {
        val (red, green, blue) = pixel(frame, x, y)
        image.setRGB(x, y, (red << 16) | (green << 8) | blue)
      }
      val _ = ImageIO.write(image, "png", new File(directory, s"solar-system-$name.png"))
    }
  }
}
