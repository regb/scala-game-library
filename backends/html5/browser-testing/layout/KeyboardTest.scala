package sgl.html5.testing.layout

import sgl.html5.testing.{BrowserSuite, Key, Page}

/** Keys, and who gets them: a game, or the page around it. And buttons and scrolling. */
class KeyboardTest extends BrowserSuite {

  private def withGame(site: String)(body: Page => Unit): Unit =
    withSite(s"backends/html5/browser-testing/layout/${site}_site") { (page, url) =>
      page.resize(900, 700)
      page.open(url)
      page.waitUntil("the game starting")(page.console.contains("started"))
      page.nextFrame()
      body(page)
      assert(page.errors.isEmpty)
    }

  // The console comes later than what wrote to it: it is complete up to a line
  // written after.
  private var marks = 0
  private def settle(page: Page): Unit = {
    marks += 1
    val mark = s"mark $marks"
    page.nextFrame()
    val _ = page.evaluate(s"console.log('$mark')")
    page.waitUntil(mark)(page.console.contains(mark))
  }

  /** The lines about keys since the last time. */
  private def keys(page: Page): Vector[String] = {
    settle(page)
    val lines = page.console.filter(_.startsWith("key")).toVector
    page.clear()
    lines
  }

  private def focus(page: Page, selector: String): Vector[String] = {
    val (x, y) = page.box(selector).at(0.5, 0.5)
    page.mouse.click(x, y)
    keys(page)
  }

  private def typed(page: Page): String = page.evaluate("document.getElementById('field').value").string

  for(site <- Vector("canvas-focused", "webgl-focused")) {
    test(s"$site: a form around a game works, and the game has its keys once it is clicked") {
      withGame(site) { page =>
        // Nothing was clicked yet.
        page.keyboard.typeLetter('a')
        assert(keys(page).isEmpty)

        val _ = focus(page, "#field")
        page.keyboard.typeLetter('b')
        page.keyboard.down(Key.Enter)
        page.keyboard.up(Key.Enter)
        assert(keys(page).isEmpty && typed(page) == "b")

        val _ = focus(page, "#game")
        page.keyboard.typeLetter('c')
        assert(keys(page) == Vector("key down C", "key up C") && typed(page) == "b")

        val _ = focus(page, "#field")
        page.keyboard.typeLetter('d')
        assert(keys(page).isEmpty && typed(page) == "bd")
      }
    }

    test(s"$site: a key that is held is released when the game loses the focus") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        page.keyboard.down(Key.Left)
        assert(keys(page) == Vector("key down Left"))
        assert(focus(page, "#field") == Vector("key up Left"))
        page.keyboard.up(Key.Left)
        assert(keys(page).isEmpty)
        val _ = focus(page, "#game")
        page.keyboard.down(Key.Left)
        assert(keys(page) == Vector("key down Left"))
      }
    }

    test(s"$site: keys with the same name are told apart") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        page.keyboard.down(Key.LeftShift)
        page.keyboard.down(Key.RightShift)
        page.keyboard.up(Key.LeftShift)
        assert(keys(page) == Vector("key down LeftShift", "key down RightShift", "key up LeftShift"))
        page.keyboard.up(Key.RightShift)
        assert(keys(page) == Vector("key up RightShift"))
        page.keyboard.down(Key.Enter)
        page.keyboard.down(Key.NumpadEnter)
        assert(keys(page) == Vector("key down Enter", "key down NumpadEnter"))
      }
    }

    test(s"$site: every mouse button is told apart") {
      withGame(site) { page =>
        val (x, y) = page.box("#game").at(0.5, 0.5)
        Vector("left", "middle", "right", "back", "forward").foreach(page.mouse.press(x, y, _))
        settle(page)
        val buttons = page.console.filter(_.startsWith("mouse down")).map(_.split(' ')(2)).toVector
        assert(buttons == Vector("Left", "Middle", "Right", "Other(3)", "Other(4)"))
      }
    }

    test(s"$site: scrolling comes as the browser says, over both axes") {
      withGame(site) { page =>
        val (x, y) = page.box("#game").at(0.5, 0.5)
        page.mouse.wheel(x, y, deltaY = 120, deltaX = -30)
        def scrolled = page.console.find(_.startsWith("scrolled"))
        page.waitUntil("scrolling")(scrolled.nonEmpty)
        // JavaScript writes a whole number without its fraction.
        assert(scrolled.contains("scrolled -30 120 Pixels"))
      }
    }
  }

  // These do not set keyboard_capture.
  for(site <- Vector("canvas-fixed", "webgl-fixed")) {
    test(s"$site: a game takes its keys from the whole page by default") {
      withGame(site) { page =>
        // Nothing was clicked yet.
        page.keyboard.typeLetter('a')
        assert(keys(page) == Vector("key down A", "key up A"))

        val _ = focus(page, "#field")
        page.keyboard.typeLetter('b')
        page.keyboard.down(Key.Enter)
        page.keyboard.up(Key.Enter)
        assert(keys(page) == Vector("key down B", "key up B", "key down Enter", "key up Enter"))
        assert(typed(page) == "")
      }
    }
  }
}
