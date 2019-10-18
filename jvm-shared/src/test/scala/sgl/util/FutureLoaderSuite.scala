package sgl.util

import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class FutureLoaderSuite extends LoaderAbstractSuite {

  override def makeLoader[A](body: => A): Loader[A] = FutureLoader(body)

}
