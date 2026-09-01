package sgl.desktop

import org.scalatest.funsuite.AnyFunSuite
import sgl.CapturedFrame

import java.nio.file.Files
import javax.imageio.ImageIO

class DesktopFrameIOSuite extends AnyFunSuite {
  test("writePng preserves canonical RGBA pixels") {
    val frame = new CapturedFrame(
      width = 2,
      height = 2,
      rgba = Array[Byte](
        255.toByte, 0, 0, 255.toByte,
        0, 255.toByte, 0, 128.toByte,
        0, 0, 255.toByte, 64,
        255.toByte, 255.toByte, 255.toByte, 0
      )
    )
    val directory = Files.createTempDirectory("sgl-frame-capture")
    val path = directory.resolve("nested/frame.png")

    try {
      assert(DesktopFrameIO.writePng(frame, path) == path)
      val image = ImageIO.read(path.toFile)
      assert(image.getWidth == 2)
      assert(image.getHeight == 2)
      assert(image.getRGB(0, 0) == 0xffff0000)
      assert(image.getRGB(1, 0) == 0x8000ff00)
      assert(image.getRGB(0, 1) == 0x400000ff)
      assert(image.getRGB(1, 1) == 0x00ffffff)
    } finally {
      Files.deleteIfExists(path)
      Files.deleteIfExists(path.getParent)
      Files.deleteIfExists(directory)
    }
  }
}
