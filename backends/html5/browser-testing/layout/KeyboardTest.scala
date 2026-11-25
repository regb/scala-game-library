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

  /** What was typed in the game since the last time. */
  private def text(page: Page): Vector[String] = {
    settle(page)
    val lines = page.console.filter(_.startsWith("typed ")).map(_.stripPrefix("typed ")).toVector
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

    test(s"$site: the keys that edit, modifiers and function keys") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        val pressed = Vector(
          Key.LeftControl, Key.RightControl, Key.LeftAlt, Key.RightAlt, Key.Delete, Key.Home, Key.End, Key.PageUp, Key.PageDown,
          Key.function(1), Key.function(10)
        )
        pressed.foreach { key =>
          page.keyboard.down(key)
          page.keyboard.up(key)
        }
        assert(keys(page).filter(_.startsWith("key down")).map(_.stripPrefix("key down ")) == Vector(
          "LeftControl", "RightControl", "LeftAlt", "RightAlt", "Delete", "Home", "End", "PageUp", "PageDown", "F1", "F10"
        ))
        assert(text(page).isEmpty)
      }
    }

    test(s"$site: the browser keeps its own keys") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        val _ = page.evaluate("window.kept = []; window.addEventListener('keydown', e => window.kept.push(e.key + ' ' + e.defaultPrevented)); 0")
        Vector(Key.function(11), Key.function(12), Key.function(4)).foreach { key =>
          page.keyboard.down(key)
          page.keyboard.up(key)
        }
        assert(page.evaluate("window.kept.join()").string == "F11 false,F12 false,F4 true")
        assert(keys(page) == Vector("key down F4", "key up F4"))
        // Out of the game.
        assert(page.evaluate("document.activeElement.id").string == "game")
        page.keyboard.down(Key.Tab)
        page.keyboard.up(Key.Tab)
        assert(page.evaluate("document.activeElement.id").string != "game")
        assert(keys(page).isEmpty)
      }
    }

    test(s"$site: text is what is typed, and not what is a shortcut") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        page.keyboard.typeLetter('a')
        page.keyboard.typeCharacter("A", Key.letter('a'), modifiers = 8)
        page.keyboard.typeCharacter("é", Key("é", "Digit2", 50))
        page.keyboard.typeCharacter("\ud83c\udf19", Key.letter('m'))
        page.keyboard.typeCharacter(" ", Key.Space)
        assert(text(page) == Vector("a", "A", "é", "\ud83c\udf19", " "))
        for(modifier <- Vector(1, 2, 4, 3)) page.keyboard.typeCharacter("x", Key.letter('x'), modifiers = modifier)
        Vector(Key.Enter, Key.Backspace, Key.Left, Key.LeftShift).foreach { key =>
          page.keyboard.down(key)
          page.keyboard.up(key)
        }
        assert(text(page).isEmpty)
      }
    }

    test(s"$site: what the game does not use of what is typed is for the page") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        val _ = page.evaluate("window.kept = []; window.addEventListener('keydown', e => window.kept.push(e.key + ' ' + e.defaultPrevented)); 0")
        page.keyboard.typeCharacter("'", Key.Quote)
        page.keyboard.typeCharacter("ü", Key.Quote)
        assert(page.evaluate("window.kept.join()").string == "' false,ü true")
        assert(text(page) == Vector("'", "ü"))
      }
    }

    test(s"$site: a key that is held goes down once, and repeats") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        page.keyboard.down(Key.Backspace)
        page.keyboard.repeat(Key.Backspace)
        page.keyboard.repeat(Key.Backspace)
        page.keyboard.up(Key.Backspace)
        assert(keys(page) == Vector("key down Backspace", "key repeated Backspace", "key repeated Backspace", "key up Backspace"))
        page.keyboard.typeCharacter("a", Key.letter('a'), repeats = 1)
        settle(page)
        assert(page.console.take(5).toVector == Vector("key down A", "typed a", "key repeated A", "typed a", "key up A"))
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

  // Only canvas-focused sets keyboard_prevent_shortcuts.
  for(site <- Vector("canvas-fixed", "canvas-focused", "webgl-fixed", "webgl-focused")) {
    test(s"$site: the game has keys pressed with Ctrl or Alt, and the browser does too unless the game says") {
      withGame(site) { page =>
        val _ = focus(page, "#game")
        // After the game's own listeners.
        page.evaluate("window.prevented = []; window.addEventListener('keydown', e => prevented.push(e.defaultPrevented)); 1")
        val d = Key.letter('d')
        page.keyboard.down(d)
        page.keyboard.up(d)
        page.keyboard.down(d, modifiers = 2)
        page.keyboard.up(d, modifiers = 2)
        page.keyboard.down(d, modifiers = 1)
        page.keyboard.up(d, modifiers = 1)
        assert(keys(page) == Vector.fill(3)(Vector("key down D", "key up D")).flatten)
        val shortcuts = site == "canvas-focused"
        assert(page.evaluate("prevented.join(' ')").string == s"true $shortcuts $shortcuts")
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
