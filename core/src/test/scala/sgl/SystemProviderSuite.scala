package sgl

import org.scalatest.funsuite.AnyFunSuite

class SystemProviderSuite extends AnyFunSuite {

  object InstrumentedSystemProvider extends TestSystemProvider {
    var instrumentedUri: java.net.URI = null
    class InstrumentedTestSystem extends TestSystem {
      override def openWebpage(uri: java.net.URI): Unit = {
        instrumentedUri = uri
      }
    }
    override val System = new InstrumentedTestSystem
  }

  test("openGooglePlayApp defaults to the correct URL without parameters") {
    InstrumentedSystemProvider.System.openGooglePlayApp("com.regblanc.sgl")
    val want = new java.net.URI("https://play.google.com/store/apps/details?id=com.regblanc.sgl")
    assert(InstrumentedSystemProvider.instrumentedUri == want)
  }

  test("openGooglePlayApp defaults to the correct URL with parameters") {
    InstrumentedSystemProvider.System.openGooglePlayApp("com.regblanc.sgl", Map("a" -> "b"))
    val want = new java.net.URI("https://play.google.com/store/apps/details?id=com.regblanc.sgl&a=b")
    assert(InstrumentedSystemProvider.instrumentedUri == want)
    InstrumentedSystemProvider.System.openGooglePlayApp("com.regblanc.sgl", Map("a" -> "b", "c" -> "d"))
    val want2 = new java.net.URI("https://play.google.com/store/apps/details?id=com.regblanc.sgl&a=b&c=d")
    assert(InstrumentedSystemProvider.instrumentedUri == want2)
  }


  object PartsResourcePathSystemProvider extends TestSystemNoResourcePathProvider with PartsResourcePathProvider {
    override val ResourcesRoot = PartsResourcePath(Vector("root"))
  }

  test("PartsResourcePath creates the correct path") {
    val r = PartsResourcePathSystemProvider.ResourcesRoot / "a" / "b" / "c.txt"
    assert(r.path === "root/a/b/c.txt")
  }

  test("PartsResourcePath creates the correct pathes with multiple filenames") {
    val rs = PartsResourcePathSystemProvider.ResourcesRoot / "a" / "b" / Seq("c.txt", "d.txt")
    assert(rs.size === 2)
    assert(rs(0).path === "root/a/b/c.txt")
    assert(rs(1).path === "root/a/b/d.txt")
  }

  test("PartsResourcePath returns proper extension") {
    val r = PartsResourcePathSystemProvider.ResourcesRoot / "a" / "b" / "c.txt"
    assert(r.extension === Some("txt"))
    val r2 = PartsResourcePathSystemProvider.ResourcesRoot / "a" / "b" / "c"
    assert(r2.extension === None)
  }
}
