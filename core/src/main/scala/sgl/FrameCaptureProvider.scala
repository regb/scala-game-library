package sgl

import sgl.util.{DefaultLoader, Loader}

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/** A rendered frame in canonical, backend-independent form.
  *
  * Pixels are tightly packed RGBA8, start at the top-left corner, and proceed
  * left-to-right then top-to-bottom. Color channels are non-premultiplied. The
  * frame owns `rgba`; callers must not mutate it.
  */
final class CapturedFrame(val width: Int, val height: Int, val rgba: Array[Byte]) {
  require(width > 0, "Captured frame width must be positive")
  require(height > 0, "Captured frame height must be positive")
  require(rgba.length == width * height * CapturedFrame.BytesPerPixel,
    s"Expected ${width * height * CapturedFrame.BytesPerPixel} RGBA bytes, got ${rgba.length}")

  val rowStride: Int = width * CapturedFrame.BytesPerPixel
}

object CapturedFrame {
  val BytesPerPixel: Int = 4
}

/** Captures the next complete frame rendered after a request.
  *
  * Backends take pending requests at the beginning of a frame and complete
  * them after all game rendering for that frame has finished. Several requests
  * waiting for the same frame share one pixel readback.
  */
trait FrameCaptureProvider {
  protected final class FrameCaptureBatch private[FrameCaptureProvider] (
      private[FrameCaptureProvider] val promises: Vector[DefaultLoader[CapturedFrame]]) {
    def requested: Boolean = promises.nonEmpty
  }

  private val frameCaptureLock = new Object
  private val pendingFrameCaptures = ArrayBuffer.empty[DefaultLoader[CapturedFrame]]

  final def captureNextFrame(): Loader[CapturedFrame] = frameCaptureLock.synchronized {
    val promise = new DefaultLoader[CapturedFrame]
    pendingFrameCaptures += promise
    promise.loader
  }

  /** Takes requests which must be fulfilled by the frame about to be rendered. */
  protected final def beginFrameCapture(): FrameCaptureBatch = frameCaptureLock.synchronized {
    val batch = new FrameCaptureBatch(pendingFrameCaptures.toVector)
    pendingFrameCaptures.clear()
    batch
  }

  protected final def completeFrameCapture(batch: FrameCaptureBatch, frame: => CapturedFrame): Unit = {
    if(batch.requested) {
      val result = Try(frame)
      batch.promises.foreach(_.tryComplete(result))
    }
  }

  protected final def failFrameCapture(batch: FrameCaptureBatch, error: Throwable): Unit =
    batch.promises.foreach(_.tryFailure(error))
}
