package sgl.util

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Failure}

class DefaultLoaderSuite extends AnyFunSuite {
  
  // TODO: with LoaderAbstractSuite {
  //   Try to run all the abstract loader suite test, but they don't seem to work
  //   due to the sleep behavior.
  // override def makeLoader[A](body: => A): Loader[A] = {
  //   val p = new DefaultLoader[A]
  //   try {
  //     p.success(body)
  //   } catch {
  //     case (e: Exception) => p.failure(e)
  //   }
  //   p.loader
  // }

  test("successful returns a loaded Loader with correct content") {
    val l = Loader.successful(13)
    assert(l.isLoaded)
    assert(l.value.get.get === 13)
  }

  test("failed returns a loaded Loader with correct content") {
    val l = Loader.failed[Int](new RuntimeException)
    assert(l.isLoaded)
    assert(l.value.get.isInstanceOf[Failure[Int]])
  }

  test("combine of successful loaders returns a loaded loader with correct content") {
    val l1 = Loader.successful(1)
    val l2 = Loader.successful(2)
    val l3 = Loader.successful(3)

    val l = Loader.combine(Seq(l1, l2, l3))

    assert(l.isLoaded)
    assert(l.value.get.get === Seq(1,2,3))
  }

  test("combine with one failed loader returns a loaded loader with a failure") {
    val l1 = Loader.successful(1)
    val l2 = Loader.successful(2)
    val l3 = Loader.failed(new RuntimeException)

    val l = Loader.combine(Seq(l1, l2, l3))

    assert(l.isLoaded)
    assert(l.value.get.isInstanceOf[Failure[Seq[Int]]])
  }

  test("combine with multiple failed loaders returns a loaded loader with a failure") {
    val l1 = Loader.successful(1)
    val l2 = Loader.failed(new RuntimeException)
    val l3 = Loader.failed(new RuntimeException)

    val l = Loader.combine(Seq(l1, l2, l3))

    assert(l.isLoaded)
    assert(l.value.get.isInstanceOf[Failure[Seq[Int]]])
  }

}
