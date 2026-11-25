package sgl.html5.testing.audio

import sgl.html5.testing.{BrowserSuite, Key, Page}

/** Music and sounds, in a browser that only plays them once the player did something. */
class AudioTest extends BrowserSuite {
  override protected def allowAutoplay: Boolean = false

  private val escape = Key("Escape", "Escape", 27)

  private def withGame(body: Page => Unit): Unit =
    withSite("backends/html5/browser-testing/audio/game_site") { (page, url) =>
      page.open(url)
      page.waitUntil("the game starting")(page.console.contains("ready"))
      page.nextFrame()
      assert(!musicPlays(page))
      body(page)
    }

  private def press(page: Page, key: Key): Unit = {
    page.keyboard.down(key)
    page.keyboard.up(key)
    page.nextFrame()
  }

  // The first one is the music.
  private def musicPlays(page: Page): Boolean =
    page.evaluate("!document.querySelector('audio').paused").boolean
  private def soundTags(page: Page): Int = page.evaluate("document.querySelectorAll('audio').length - 1").int

  private def starts(page: Page): Unit = {
    page.waitUntil("the music playing")(musicPlays(page))
    assert(page.errors.isEmpty)
  }

  test("music asked for at the start plays from a click") {
    withGame { page =>
      page.mouse.click(50, 50)
      starts(page)
    }
  }

  test("music asked for at the start plays from a key") {
    withGame { page =>
      press(page, Key.Space)
      starts(page)
    }
  }

  // Browsers count the end of a touch, not its start.
  test("music asked for at the start plays from a touch") {
    withGame { page =>
      page.touch.start(50, 50)
      page.nextFrame()
      assert(page.errors.isEmpty)
      page.touch.end()
      starts(page)
    }
  }

  // Browsers do not count Escape.
  test("music asked for at the start plays from a key after Escape") {
    withGame { page =>
      press(page, escape)
      assert(page.errors.isEmpty && !musicPlays(page))
      press(page, Key.Space)
      starts(page)
    }
  }

  test("music that was paused before it could play stays so, until it is played") {
    withGame { page =>
      // Neither key is one that makes the browser play.
      page.evaluate("window.dispatchEvent(new Event('focus'))")
      page.evaluate("document.dispatchEvent(new KeyboardEvent('keydown', {key: 'p', code: 'KeyP', keyCode: 80}))")
      page.mouse.click(50, 50)
      page.nextFrame()
      Thread.sleep(200)
      assert(!musicPlays(page))
      press(page, Key.letter('m'))
      starts(page)
    }
  }

  test("a sound that the browser does not play yet is dropped, and can be played later") {
    withGame { page =>
      (1 to 3).foreach { i =>
        page.evaluate(s"document.dispatchEvent(new KeyboardEvent('keydown', {key: 's', code: 'KeyS', keyCode: 83}))")
        page.evaluate(s"document.dispatchEvent(new KeyboardEvent('keyup', {key: 's', code: 'KeyS', keyCode: 83}))")
        page.nextFrame()
      }
      assert(page.errors.isEmpty)
      // The same one each time, as it is free again.
      assert(soundTags(page) == 1)
      page.mouse.click(50, 50)
      press(page, Key.letter('s'))
      page.waitUntil("the sound playing")(page.evaluate("document.querySelectorAll('audio')[1].currentTime > 0").boolean)
      assert(page.errors.isEmpty)
    }
  }
}
