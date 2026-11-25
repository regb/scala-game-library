package sgl.html5.testing.layout

import java.net.URLEncoder

import sgl.html5.testing.{Box, BrowserSuite, Page}

/** Size and input of the canvas, however it is laid out in its page. */
class LayoutTest extends BrowserSuite {

  // The canvas of every game but the fullscreen ones.
  private val (width, height) = (300, 200)

  private final case class Around(name: String, wrapper: String, canvas: String)
  private val plain = Around("plain", "", "")
  private val arounds = Vector(
    Around("inside a box, scrolled", "margin: 150px 0 1500px 70px; padding: 20px", ""),
    Around("with a border and padding", "margin: 50px", "border: 10px solid red; padding: 15px 5px; box-sizing: border-box"),
    Around("scaled by a transform", "margin: 50px; transform: scale(0.5); transform-origin: 0 0", "")
  )

  // A screen with two pixels to a CSS pixel, which also shows the two confused.
  private val pixelRatio = 2.0

  private def withGame(site: String, around: Around)(body: Page => Unit): Unit =
    withSite(s"backends/html5/browser-testing/layout/${site}_site") { (page, url) =>
      page.resize(900, 700, pixelRatio)
      def encode(css: String) = URLEncoder.encode(css, "UTF-8")
      page.open(s"$url?wrapper=${encode(around.wrapper)}&canvas=${encode(around.canvas)}")
      page.waitUntil("the game starting")(page.console.contains("started"))
      page.nextFrame()
      body(page)
      assert(page.errors.isEmpty)
    }

  private final case class Canvas(cssWidth: Double, cssHeight: Double, width: Int, height: Int, shown: Box)

  // What is drawn of the canvas: its size before transforms, and where it is shown.
  private def canvas(page: Page): Canvas = {
    val c = page.evaluate("""(() => {
      const c = document.getElementById("game"), r = c.getBoundingClientRect(), s = getComputedStyle(c);
      const p = side => parseFloat(s["padding" + side]), scale = r.width / c.offsetWidth;
      const w = c.clientWidth - p("Left") - p("Right"), h = c.clientHeight - p("Top") - p("Bottom");
      return {w, h, bw: c.width, bh: c.height,
        x: r.x + scale * (c.clientLeft + p("Left")), y: r.y + scale * (c.clientTop + p("Top")), sw: scale * w, sh: scale * h};
    })()""")
    Canvas(c("w").double, c("h").double, c("bw").int, c("bh").int, Box(c("x").double, c("y").double, c("sw").double, c("sh").double))
  }

  private def checkSize(page: Page, expectedWidth: Int, expectedHeight: Int): Unit = {
    val c = canvas(page)
    assert((c.cssWidth, c.cssHeight) == ((expectedWidth.toDouble, expectedHeight.toDouble)))
    assert((c.width, c.height) == (((expectedWidth * pixelRatio).toInt, (expectedHeight * pixelRatio).toInt)))
  }

  // Waits for a line of the console such as "mouse down 10 20" and returns its position.
  private def reported(page: Page, what: String): (Int, Int) = {
    def line = page.console.find(_.startsWith(what))
    page.waitUntil(what)(line.nonEmpty)
    line.get.split(' ').takeRight(2).map(_.toInt) match { case Array(x, y) => (x, y) }
  }

  private def checkInput(page: Page): Unit = {
    val c = canvas(page)
    // The browser reports whole CSS pixels, which can stand for several of the canvas.
    val tolerance = 2.0 * c.width / c.shown.width + 1.0
    for((fx, fy) <- Vector((0.25, 0.25), (0.8, 0.5))) {
      val (x, y) = c.shown.at(fx, fy)
      def check(position: (Int, Int)): Unit = {
        assert(math.abs(position._1 - fx * c.width) <= tolerance && math.abs(position._2 - fy * c.height) <= tolerance,
          s"got $position, expected about (${fx * c.width}, ${fy * c.height})")
      }
      page.clear()
      page.mouse.click(x, y)
      check(reported(page, "mouse down"))
      check(reported(page, "mouse up"))
      page.clear()
      page.touch.start(x, y)
      check(reported(page, "touch down"))
      page.touch.end()
      check(reported(page, "touch up"))
    }
  }

  for(backend <- Vector("canvas", "webgl")) {
    def name(what: String) = s"$backend: $what"

    for(layout <- Vector("fixed", "provided"); around <- arounds) {
      // The page gives the size of a provided canvas.
      val sized =
        if(layout == "fixed") around
        else around.copy(canvas = s"${around.canvas}; box-sizing: content-box; width: ${width}px; height: ${height}px")

      test(name(s"a $layout canvas ${around.name} keeps its size and gets input where it is shown")) {
        withGame(s"$backend-$layout", sized) { page =>
          checkSize(page, width, height)
          Vector(800, 850, 900).foreach(page.resize(_, 700, pixelRatio))
          checkSize(page, width, height)
          checkInput(page)
          if(around.name.contains("scrolled")) {
            page.evaluate("scrollTo(0, 100)")
            page.nextFrame()
            checkInput(page)
          }
        }
      }
    }

    test(name("a centered canvas stays in the middle of the window")) {
      withGame(s"$backend-centered", plain) { page =>
        for((w, h) <- Vector((900, 700), (600, 800))) {
          page.resize(w, h, pixelRatio)
          checkSize(page, width, height)
          assert(page.box("#game") == Box((w - width) / 2.0, (h - height) / 2.0, width.toDouble, height.toDouble))
          checkInput(page)
        }
      }
    }

    test(name("a fullscreen canvas follows the size of the window")) {
      withGame(s"$backend-fullscreen", plain) { page =>
        for((w, h) <- Vector((900, 700), (600, 800), (1000, 500))) {
          page.resize(w, h, pixelRatio)
          checkSize(page, w, h)
          checkInput(page)
        }
      }
    }

    test(name("a mouse button released outside of the canvas is released in the game")) {
      withGame(s"$backend-fixed", plain) { page =>
        val (x, y) = canvas(page).shown.at(0.5, 0.5)
        page.mouse.down(x, y)
        reported(page, "mouse down")
        page.mouse.move(850, 650)
        page.mouse.up(850, 650)
        reported(page, "mouse up")
        page.nextFrame()
        assert(page.console.count(_.startsWith("mouse up")) == 1)
      }
    }
  }
}
