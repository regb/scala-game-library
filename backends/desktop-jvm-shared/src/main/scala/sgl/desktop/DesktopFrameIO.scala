package sgl.desktop

import sgl.{Application, CapturedFrame, FrameCaptureProvider}

import java.awt.image.BufferedImage
import java.nio.file.{Files, Path, Paths}
import javax.imageio.ImageIO
import scala.util.{Failure, Success}

/** JVM encoders for backend-independent captured frames. */
object DesktopFrameIO {
  def writePng(frame: CapturedFrame, path: Path): Path = {
    val parent = path.toAbsolutePath.getParent
    if(parent != null) Files.createDirectories(parent)

    val image = new BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB)
    val argb = new Array[Int](frame.width * frame.height)
    var pixel = 0
    var offset = 0
    while(pixel < argb.length) {
      val red = frame.rgba(offset) & 0xff
      val green = frame.rgba(offset + 1) & 0xff
      val blue = frame.rgba(offset + 2) & 0xff
      val alpha = frame.rgba(offset + 3) & 0xff
      argb(pixel) = (alpha << 24) | (red << 16) | (green << 8) | blue
      pixel += 1
      offset += CapturedFrame.BytesPerPixel
    }
    image.setRGB(0, 0, frame.width, frame.height, argb, 0, frame.width)

    if(!ImageIO.write(image, "png", path.toFile))
      throw new IllegalStateException("No PNG ImageIO writer is available")
    path
  }
}

/** Opt-in screenshot hook used by desktop examples.
  *
  * Set `SGL_SCREENSHOT_PATH` or the `sgl.screenshot.path` system property. The
  * application waits for an optional warm-up period, requests the following
  * frame, and writes it as PNG without changing portable game code.
  */
trait SaveScreenshotFromProperty extends Application with FrameCaptureProvider {

  private var screenshotRequested = false
  private var renderedFrames = 0

  private lazy val screenshotPath: Option[Path] =
    Option(java.lang.System.getProperty("sgl.screenshot.path"))
      .orElse(Option(java.lang.System.getenv("SGL_SCREENSHOT_PATH")))
      .filter(_.nonEmpty)
      .map(Paths.get(_))

  private lazy val screenshotDelayFrames: Int =
    Option(java.lang.System.getProperty("sgl.screenshot.delayFrames"))
      .orElse(Option(java.lang.System.getenv("SGL_SCREENSHOT_DELAY_FRAMES")))
      .map(_.toInt)
      .getOrElse(30)

  abstract override def frame(dt: Double): Unit = {
    super.frame(dt)
    renderedFrames += 1
    if(!screenshotRequested && renderedFrames >= screenshotDelayFrames) {
      screenshotPath.foreach { path =>
        screenshotRequested = true
        captureNextFrame().onLoad {
          case Success(frame) =>
            DesktopFrameIO.writePng(frame, path)
            println(s"Captured frame to ${path.toAbsolutePath}")
          case Failure(error) =>
            java.lang.System.err.println(s"Could not capture frame: ${error.getMessage}")
        }
      }
    }
  }
}
