package sgl.examples.liveconfig

import java.awt.image.BufferedImage
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, StandardCopyOption}
import javax.imageio.ImageIO

import org.scalatest.funsuite.AnyFunSuite
import sgl.{CapturedFrame, DesktopSystemProvider}
import sgl.desktop.DesktopAssetDirectory
import sgl.headless.opengl.HeadlessOpenGLApp
import sgl.util.NoLoggingProvider

/** Runs the example with its assets in a directory, changes them while it
  * runs, and looks at what it draws.
  */
class HotReloadHeadlessSuite extends AnyFunSuite {

  private val Width = 640
  private val Height = 400

  test("a changed image, level, and settings file show without a restart") {
    val directory = Files.createTempDirectory("sgl-hot-reload")
    Files.createDirectories(directory.resolve("raw-image"))
    Files.createDirectories(directory.resolve("text"))
    writeImage(directory, "raw-image/hero.png", 0xff2020)
    // The hero stands still in the middle of the screen.
    writeText(directory, "text/tuning.properties", "hero.speed=0\nhero.size=64\nbackground=#102030\n")
    writeText(directory, "text/level.txt", "########\n#R.....#\n########\n")

    object app extends HeadlessOpenGLApp
        with core.AbstractApp
        with core.GeneratedAssetsProvider
        with DesktopSystemProvider
        with DesktopAssetDirectory
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (Width, Height)
      override protected def desktopAssetDirectory: Option[Path] = Some(directory)
      override protected def desktopAssetPollMillis: Long = 0L
    }

    app.start()
    try {
      val hero = (Width / 2, Height / 2)
      // The level is 8 x 3 blocks of 32 pixels, centered: the R block is at
      // column 1, row 1, left of the hero.
      val block = (Width / 2 - 128 + 32 + 16, Height / 2)
      val corner = (Width - 3, Height - 3)

      var frame = waitFor(app, "the scene to appear")(f => isRed(pixel(f, hero._1, hero._2)))
      save(frame, "before")
      assert(isRed(pixel(frame, block._1, block._2)), s"expected the red block, got ${pixel(frame, block._1, block._2)}")
      assert(pixel(frame, corner._1, corner._2) == ((0x10, 0x20, 0x30)))

      // The image: no game code is involved, the texture handle shows the new one.
      writeImage(directory, "raw-image/hero.png", 0x2040ff)
      frame = waitFor(app, "the new hero image")(f => isBlue(pixel(f, hero._1, hero._2)))
      assert(isRed(pixel(frame, block._1, block._2)))

      // The level, a LiveAsset read every frame.
      writeText(directory, "text/level.txt", "########\n#G.....#\n########\n")
      frame = waitFor(app, "the new level")(f => isGreen(pixel(f, block._1, block._2)))

      // Settings: the background color and the hero's size.
      writeText(directory, "text/tuning.properties", "hero.speed=0\nhero.size=200\nbackground=#304050\n")
      frame = waitFor(app, "the new settings")(f => pixel(f, corner._1, corner._2) == ((0x30, 0x40, 0x50)))
      assert(app.tuning.size.get == 200.0)
      // The hero is now big enough to cover the block.
      assert(isBlue(pixel(frame, block._1, block._2)))
      save(frame, "after")

      // A broken settings file is rejected; what was there stays.
      writeText(directory, "text/tuning.properties", "hero.speed=-5\nbackground=#ffffff\n")
      (0 until 10).foreach(_ => step(app))
      frame = capture(app)
      assert(pixel(frame, corner._1, corner._2) == ((0x30, 0x40, 0x50)))
      assert(app.tuning.speed.get == 0.0 && app.tuning.size.get == 200.0)
    } finally {
      app.stop()
      deleteRecursively(directory)
    }
  }

  private def step(app: HeadlessOpenGLApp): Unit = {
    app.step(1.0 / 60.0)
    Thread.sleep(5)
  }

  private def waitFor(app: HeadlessOpenGLApp, what: String)(done: CapturedFrame => Boolean): CapturedFrame = {
    val deadline = java.lang.System.nanoTime() + 20_000_000_000L
    var frame = capture(app)
    while(!done(frame)) {
      if(java.lang.System.nanoTime() > deadline) {
        save(frame, "timeout")
        fail(s"timed out waiting for $what")
      }
      step(app)
      frame = capture(app)
    }
    frame
  }

  // Every write gets a later modification time than the previous one, so a
  // poll in the same millisecond still sees it.
  private var clock = java.lang.System.currentTimeMillis() - 60_000L
  private def touch(file: Path): Unit = {
    clock += 1000L
    Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(clock))
    ()
  }

  // Written next to the file, then moved over it, as editors save.
  private def writeText(directory: Path, path: String, text: String): Unit = {
    val target = directory.resolve(path)
    val temporary = directory.resolve(path + ".tmp")
    Files.write(temporary, text.getBytes(UTF_8))
    touch(temporary)
    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    ()
  }

  private def writeImage(directory: Path, path: String, rgb: Int): Unit = {
    val image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    for(y <- 0 until 16; x <- 0 until 16) image.setRGB(x, y, 0xff000000 | rgb)
    val target = directory.resolve(path)
    val temporary = directory.resolve(path + ".tmp")
    ImageIO.write(image, "png", temporary.toFile)
    touch(temporary)
    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    ()
  }

  private def isRed(c: (Int, Int, Int)): Boolean = c._1 > 180 && c._2 < 90 && c._3 < 90
  private def isGreen(c: (Int, Int, Int)): Boolean = c._2 > 150 && c._1 < 100 && c._3 < 120
  private def isBlue(c: (Int, Int, Int)): Boolean = c._3 > 180 && c._1 < 90

  private def capture(app: HeadlessOpenGLApp): CapturedFrame = {
    val pending = app.captureNextFrame()
    app.step(1.0 / 60.0)
    pending.value.flatMap(_.toOption).getOrElse(throw new IllegalStateException("Headless frame capture did not complete"))
  }

  private def pixel(frame: CapturedFrame, x: Int, y: Int): (Int, Int, Int) = {
    val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
    (frame.rgba(offset) & 0xff, frame.rgba(offset + 1) & 0xff, frame.rgba(offset + 2) & 0xff)
  }

  // Kept with the test outputs, to look at when the test fails.
  private def save(frame: CapturedFrame, name: String): Unit =
    Option(java.lang.System.getenv("TEST_UNDECLARED_OUTPUTS_DIR")).foreach { directory =>
      val image = new BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_RGB)
      for(y <- 0 until frame.height; x <- 0 until frame.width) {
        val (red, green, blue) = pixel(frame, x, y)
        image.setRGB(x, y, (red << 16) | (green << 8) | blue)
      }
      val _ = ImageIO.write(image, "png", new File(directory, s"hot-reload-$name.png"))
    }

  private def deleteRecursively(path: Path): Unit = {
    if(Files.isDirectory(path)) {
      val children = Files.list(path)
      try children.toArray.foreach(child => deleteRecursively(child.asInstanceOf[Path])) finally children.close()
    }
    Files.deleteIfExists(path)
    ()
  }
}
