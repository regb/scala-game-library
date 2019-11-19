package sgl

import sgl.util._

import scala.util._

trait TestSystemProvider extends SystemProvider {

  class TestSystem extends System {

    def exit(): Unit = ???
    def millis(): Long = ???

    def loadText(path: ResourcePath): Loader[Array[String]] = ???

    def loadBinary(path: ResourcePath): Loader[Array[Byte]] = ???

    def openWebpage(uri: java.net.URI): Unit = ???

  }
  val System = new TestSystem

  class TestResourcePath extends AbstractResourcePath {
    def / (filename: String): ResourcePath = ???
  }
  type ResourcePath = TestResourcePath

  override val ResourcesRoot: ResourcePath = new TestResourcePath

}
