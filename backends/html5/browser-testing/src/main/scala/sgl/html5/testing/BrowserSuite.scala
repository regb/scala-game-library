package sgl.html5.testing

import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO

import scala.util.control.NonFatal

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

/** A suite of tests in a browser, which is started once for all of them.
  *
  * {{{
  * class MyGameTest extends BrowserSuite {
  *   test("the game starts without errors") {
  *     withSite("my/package/html5_site") { (page, url) =>
  *       page.open(url)
  *       page.nextFrame()
  *       assert(page.errors.isEmpty)
  *     }
  *   }
  * }
  * }}}
  */
abstract class BrowserSuite extends AnyFunSuite with BeforeAndAfterAll {
  private var started: Option[Browser] = None

  def browser: Browser = started.getOrElse {
    val launched = Browser.launch()
    started = Some(launched)
    launched
  }

  override protected def afterAll(): Unit = {
    started.foreach(_.close())
    started = None
    super.afterAll()
  }

  /** Runs a test in a new tab. If it fails, what the tab showed is kept with
    * the outputs of the test.
    */
  def withPage[A](body: Page => A): A = {
    val page = browser.newPage()
    try {
      page.resize(900, 700)
      body(page)
    } catch {
      case error: Throwable =>
        try save(page, s"failure-${System.nanoTime()}")
        catch { case NonFatal(_) => () }
        throw error
    } finally page.close()
  }

  /** The same with a site served: its path in the workspace, and then the URL
    * it is served at. The site is one of the `sites` of the test.
    */
  def withSite[A](path: String)(body: (Page, String) => A): A = {
    val site = new StaticSite(Paths.get(path))
    try withPage(body(_, site.url))
    finally site.close()
  }

  /** Keeps a screenshot with the outputs of the test. */
  def save(page: Page, name: String): Unit =
    sys.env.get("TEST_UNDECLARED_OUTPUTS_DIR").foreach { directory =>
      val _ = ImageIO.write(page.screenshot(), "png", new File(directory, s"$name.png"))
    }
}
