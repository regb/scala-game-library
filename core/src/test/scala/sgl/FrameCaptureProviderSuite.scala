package sgl

import org.scalatest.funsuite.AnyFunSuite

class FrameCaptureProviderSuite extends AnyFunSuite {
  test("capture requests complete from the next frame boundary") {
    val provider = new TestFrameCaptureProvider
    val first = provider.captureNextFrame()
    provider.begin()
    val second = provider.captureNextFrame()

    val firstFrame = solidFrame(10.toByte)
    provider.complete(firstFrame)
    assert(first.value.exists(_.get eq firstFrame))
    assert(second.value.isEmpty)

    val secondFrame = solidFrame(20.toByte)
    provider.begin()
    provider.complete(secondFrame)
    assert(second.value.exists(_.get eq secondFrame))
  }

  test("requests waiting at the same boundary share one captured frame") {
    val provider = new TestFrameCaptureProvider
    val first = provider.captureNextFrame()
    val second = provider.captureNextFrame()
    val frame = solidFrame(30.toByte)

    provider.begin()
    provider.complete(frame)

    assert(first.value.exists(_.get eq frame))
    assert(second.value.exists(_.get eq frame))
  }

  test("captured frames validate their RGBA byte count") {
    assertThrows[IllegalArgumentException](new CapturedFrame(2, 2, new Array[Byte](15)))
  }

  private def solidFrame(value: Byte): CapturedFrame =
    new CapturedFrame(1, 1, Array(value, value, value, 255.toByte))

  private final class TestFrameCaptureProvider extends FrameCaptureProvider {
    private var batch: Option[FrameCaptureBatch] = None

    def begin(): Unit = batch = Some(beginFrameCapture())

    def complete(frame: CapturedFrame): Unit = {
      completeFrameCapture(batch.get, frame)
      batch = None
    }
  }
}
