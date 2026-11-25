# Browser tests

Runs HTML5 games in a real, headless Chrome and drives them: window size, pixel
ratio, mouse, touch, keyboard, JavaScript, console, requests, and screenshots.
It speaks Chrome's DevTools protocol itself.

```bash
bazel test //backends/html5/browser-testing/layout:layout-test
```

On x86-64 Linux the browser comes with the test. `chrome/` downloads a pinned
Chrome, its libraries down to glibc, and a font, and puts them in a directory that
uses nothing of the system but the kernel. It is about 145 MB to download and 320 MB
on disk, and only happens when a browser test is built. Elsewhere the test uses an
installed Chrome or Chromium. WebGL is rendered on the CPU. To use another browser,
pass `--test_env=SGL_BROWSER=/path/to/chrome`.

To update the bundled Chrome, run `chrome/resolve.py` with a version and a date.

Chrome runs without its own sandbox, as it may be in Bazel's: only open the pages
of the test.

## Writing one

```python
load("@sgl//backends/html5/browser-testing:defs.bzl", "html5_browser_test")

html5_browser_test(
    name = "web_test",
    srcs = ["WebTest.scala"],
    sites = [":web_site"],      # made by sgl_html5_app and sgl_html5_opengl_app
)
```

```scala
import sgl.html5.testing.{BrowserSuite, Key}

class WebTest extends BrowserSuite {
  test("the game runs without errors") {
    withSite("web_site") { (page, url) =>            // its path in the workspace
      page.resize(390, 844, pixelRatio = 3)
      page.open(url)
      page.waitUntil("the game asking for its image")(page.requests.exists(_._2.endsWith("player.png")))
      val (x, y) = page.box("canvas").at(0.5, 0.5)
      page.mouse.click(x, y)
      page.keyboard.down(Key.Space)
      page.nextFrame()
      save(page, "playing")                          // in bazel-testlogs/.../test.outputs
      assert(page.errors.isEmpty)
    }
  }
}
```

A game is seen from the outside. `println` in the game ends up in `page.console`,
which is how a test can follow what happens inside, as `layout/InputProbe.scala`
does. When a test fails, a screenshot of its page is kept with the test outputs.
