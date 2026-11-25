package sgl.html5.testing.webgl

import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import scala.jdk.CollectionConverters._

import sgl.html5.testing.{BrowserSuite, Page}

class WebGLTest extends BrowserSuite {
  private val site = "backends/html5/browser-testing/webgl/game_site"
  private val (red, green, blue) = ((255, 0, 0), (0, 255, 0), (0, 0, 255))

  // The middle of the three sprites, and of what is drawn in 3D.
  private def colors(page: Page): Vector[(Int, Int, Int)] = {
    val shot = page.screenshot()
    Vector(40, 100, 160, 250).map { x =>
      val rgb = shot.getRGB(x, 50)
      ((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff)
    }
  }

  private def open(page: Page, url: String): Unit = {
    page.open(url)
    page.waitUntil("the game drawing")(colors(page).head == red)
  }

  // The loader of textures used to ignore packs, and games stayed black.
  test("a game draws packed textures mixed in one call, and blends 3D drawings") {
    withSite(site) { (page, url) =>
      open(page, url)
      val drawn = colors(page)
      assert(drawn.take(3) == Vector(red, green, red))
      val (r, g, b) = drawn.last
      assert(r == 0 && (g - 128).abs <= 2 && (b - 128).abs <= 2, drawn.last)
      assert(!page.requests.exists(_._2.endsWith("red.png")))
      assert(page.errors.isEmpty)
    }
  }

  test("a game clips, tints, and writes text") {
    withSite(site) { (page, url) =>
      open(page, url)
      val shot = page.screenshot()
      def color(x: Int, y: Int) = {
        val rgb = shot.getRGB(x, y)
        ((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff)
      }
      val clipped = for(x <- 0 until 80; y <- 72 until 100 if color(x, y) == blue) yield (x, y)
      assert(clipped == (for(x <- 20 until 60; y <- 80 until 90) yield (x, y)))
      val (r, g, b) = color(100, 90)
      assert((r - 128).abs <= 2 && g == 0 && b == 0)
      // Three bars, in what is otherwise black.
      val bars = (195 until 230).map(x => color(x, 88)._1 > 128)
      assert(bars.sliding(2).count(pair => !pair.head && pair.last) == 3, bars)
      assert(page.errors.isEmpty)
    }
  }

  // The manifest and the pack are both cached, and must be fetched again.
  test("a hot reload takes a changed image from a changed pack") {
    val copy = Files.createTempDirectory("site")
    copyTree(Paths.get(site), copy)
    withSite(copy.toString) { (page, url) =>
      open(page, url)
      val manifest = copy.resolve("static/sgl-assets-manifest.json")
      val swapped = Files.readString(manifest).replace("red.png", "swap").replace("blue.png", "red.png").replace("swap", "blue.png")
      val _ = Files.writeString(manifest, swapped)

      assert(page.evaluate("""game.reloadAssets({paths: ["raw-image/red.png"]}).then(report => report.ok)""").boolean)
      page.nextFrame()
      assert(colors(page).take(3) == Vector(blue, green, blue))
      assert(page.errors.isEmpty)
    }
  }

  private def copyTree(from: Path, to: Path): Unit = {
    val files = Files.walk(from.toRealPath())
    try files.iterator.asScala.filter(Files.isRegularFile(_)).foreach { file =>
      val target = to.resolve(from.toRealPath().relativize(file).toString)
      Files.createDirectories(target.getParent)
      Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING)
      val _ = target.toFile.setWritable(true)
    } finally files.close()
  }
}
