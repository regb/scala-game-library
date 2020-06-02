package sgl.util

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Failure}

trait LoaderAbstractSuite extends AnyFunSuite {

  def makeLoader[A](body: => A): Loader[A]

  /* 
   * TODO: How to replace the Thread.sleep in these tests with something more
   * predictable and cross-platform?
   */

  test("onLoad is called when and only when completed") {
    val l = makeLoader({
      Thread.sleep(200)
    })

    var called = false
    l.onLoad(_ => {
      called = true
    })

    Thread.sleep(100)
    assert(!called)
    assert(!l.isLoaded)
    Thread.sleep(200)
    assert(called)
    assert(l.isLoaded)
  }

  test("transform is called with the right result") {
    val l = makeLoader({
      Thread.sleep(200)
      42
    })

    var called = false
    val l2 = l.transform{
      case f@Failure(_) => {
        assert(false)
        f
      }
      case Success(n) => {
        called = true
        assert(n === 42)
        Success(n+1)
      }
    }

    Thread.sleep(100)
    assert(!called)
    assert(!l2.isLoaded)
    Thread.sleep(200)
    assert(called)
    assert(l2.isLoaded)
    assert(l2.value.get.get === 43)
  }

  test("transformWith is called with the right result") {
    val l = makeLoader({
      Thread.sleep(200)
      42
    })

    var called = false
    val l2 = l.transformWith{
      case f@Failure(_) => {
        assert(false)
        ???
      }
      case Success(n) => {
        called = true
        assert(n === 42)
        makeLoader({
          Thread.sleep(300)
          n+1
        })
      }
    }

    Thread.sleep(100)
    assert(!called)

    Thread.sleep(200)
    assert(called)
    assert(l.isLoaded)
    assert(!l2.isLoaded)

    Thread.sleep(400)
    assert(l2.isLoaded)
    assert(l2.value.get.get === 43)
  }

  test("fallbackTo returns the first successful value and ignore follow up loaders") {
    val l1 = makeLoader(42)

    var called = false
    val l2 = l1.fallbackTo(makeLoader({
      called = true
      10
    }))
    Thread.sleep(200)
    assert(l2.isLoaded)
    assert(l2.value.get.get === 42)
    assert(!called)


    // If we create the loader outside fallbackTo, it will start executing anyway.
    called = false
    val l3 = makeLoader({
      called = true
      10
    })
    val l4 = l1 fallbackTo l3
    Thread.sleep(200)
    assert(called)
    assert(l4.isLoaded)
    assert(l4.value.get.get === 42)
  }

  test("fallbackTo fallbacks to a successful value after a failed loader") {
    val l1 = makeLoader(throw new Exception("failed loader"))
    val l2 = l1.fallbackTo(makeLoader(10))
    Thread.sleep(100)
    assert(l2.isLoaded)
    assert(l2.value.get.get === 10)
  }

}
