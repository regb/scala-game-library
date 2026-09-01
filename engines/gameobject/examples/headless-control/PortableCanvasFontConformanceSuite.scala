package sgl.examples.gameobject.headlesscontrol

import org.scalatest.funsuite.AnyFunSuite
import sgl.{Application, CanvasProvider, CapturedFrame, DesktopSystemProvider, OpenGLCanvasProvider, WindowProvider}
import sgl.awt.{AWTCanvasProvider, AWTWindowProvider}
import sgl.headless.opengl.HeadlessOpenGLApp
import sgl.util.NoLoggingProvider

import java.awt.image.BufferedImage

class PortableCanvasFontConformanceSuite extends AnyFunSuite {
  private val Text = "Wrapped text uses the same metrics.\n\nSecond paragraph."
  private val LayoutWidth = 220

  test("AWT and OpenGL Canvas use identical portable font layout") {
    val awt = renderWithAwt()
    val openGL = renderWithOpenGL()

    assert(openGL.metrics == awt.metrics)
    assert(math.abs(openGL.bounds.left - awt.bounds.left) <= 1)
    assert(math.abs(openGL.bounds.top - awt.bounds.top) <= 1)
    assert(math.abs(openGL.bounds.right - awt.bounds.right) <= 1)
    assert(math.abs(openGL.bounds.bottom - awt.bounds.bottom) <= 1)
  }

  private def renderWithAwt(): RenderResult = {
    object provider extends AWTCanvasProvider
        with AWTWindowProvider
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (256, 128)
      override def withFrameCanvas[A](f: Graphics.Canvas => A): A =
        throw new UnsupportedOperationException("The conformance test supplies its own AWT canvas")
    }
    provider.gameCanvas = new java.awt.Canvas()
    provider.gameCanvas.setSize(256, 128)

    val image = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    try {
      val canvas = provider.Graphics.AWTCanvas(graphics, 256f, 128f)
      val paint = provider.Graphics.defaultPaint
        .withColor(provider.Graphics.Color.White)
        .withFont(provider.Graphics.Font.SansSerif.withSize(18))
      canvas.drawRect(0f, 0f, 256f, 128f, provider.Graphics.defaultPaint.withColor(provider.Graphics.Color.Black))
      val layout = canvas.renderText(Text, LayoutWidth, paint)
      canvas.drawText(layout, 18f, 12f)
      RenderResult(metrics(layout), coloredBounds(image))
    } finally graphics.dispose()
  }

  private def renderWithOpenGL(): RenderResult = {
    var renderedMetrics: LayoutMetrics = null

    trait FontGame extends Application {
      this: CanvasProvider with WindowProvider =>

      override def frame(dt: Double): Unit = withFrameCanvas { canvas =>
        val paint = Graphics.defaultPaint
          .withColor(Graphics.Color.White)
          .withFont(Graphics.Font.SansSerif.withSize(18))
        canvas.drawRect(0f, 0f, Window.width.toFloat, Window.height.toFloat,
          Graphics.defaultPaint.withColor(Graphics.Color.Black))
        val layout = canvas.renderText(Text, LayoutWidth, paint)
        renderedMetrics = metrics(layout)
        canvas.drawText(layout, 18f, 12f)
      }
    }

    object app extends HeadlessOpenGLApp
        with OpenGLCanvasProvider
        with FontGame
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (256, 128)
    }

    app.start()
    try {
      val capture = app.captureNextFrame()
      app.step(1.0 / 60.0)
      val frame = capture.value.flatMap(_.toOption).getOrElse(
        throw new IllegalStateException("OpenGL font frame capture did not complete")
      )
      RenderResult(renderedMetrics, coloredBounds(frame))
    } finally app.stop()
  }

  private def metrics(layout: CanvasProvider#Graphics#AbstractTextLayout): LayoutMetrics =
    LayoutMetrics(layout.lines.toVector, layout.width, layout.height, layout.lineHeight, layout.ascent, layout.descent)

  private def coloredBounds(image: BufferedImage): Bounds = {
    val pixels = image.getRGB(0, 0, image.getWidth, image.getHeight, null, 0, image.getWidth)
    bounds(image.getWidth, image.getHeight, index => {
      val color = pixels(index)
      ((color >>> 16) & 0xff) > 64 && ((color >>> 8) & 0xff) > 64 && (color & 0xff) > 64
    })
  }

  private def coloredBounds(frame: CapturedFrame): Bounds =
    bounds(frame.width, frame.height, index => {
      val offset = index * CapturedFrame.BytesPerPixel
      (frame.rgba(offset) & 0xff) > 64 &&
      (frame.rgba(offset + 1) & 0xff) > 64 &&
      (frame.rgba(offset + 2) & 0xff) > 64
    })

  private def bounds(width: Int, height: Int, colored: Int => Boolean): Bounds = {
    var left = width
    var top = height
    var right = -1
    var bottom = -1
    var y = 0
    while(y < height) {
      var x = 0
      while(x < width) {
        if(colored(y * width + x)) {
          left = scala.math.min(left, x)
          top = scala.math.min(top, y)
          right = scala.math.max(right, x)
          bottom = scala.math.max(bottom, y)
        }
        x += 1
      }
      y += 1
    }
    assert(right >= left && bottom >= top, "Expected rendered font pixels")
    Bounds(left, top, right, bottom)
  }

  private final case class LayoutMetrics(
      lines: Vector[String],
      width: Int,
      height: Int,
      lineHeight: Int,
      ascent: Int,
      descent: Int
  )
  private final case class Bounds(left: Int, top: Int, right: Int, bottom: Int)
  private final case class RenderResult(metrics: LayoutMetrics, bounds: Bounds)
}
