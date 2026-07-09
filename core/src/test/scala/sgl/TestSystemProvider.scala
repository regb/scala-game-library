package sgl

import sgl.util._

trait TestSystemProvider extends TestSystemNoResourcePathProvider {

  class TestResourcePath extends AbstractResourcePath {
    def / (filename: String): ResourcePath = ???

    override def extension: Option[String] = ???
  }
  type ResourcePath = TestResourcePath
  override val ResourcesRoot: ResourcePath = new TestResourcePath
  override val MultiDPIResourcesRoot: ResourcePath = new TestResourcePath

}

trait TestSystemNoResourcePathProvider extends SystemProvider {

  class TestSystem extends System {

    def exit(): Unit = ???
    def millis(): Long = ???

    def currentTimeMillis: Long = ???
    def nanoTime: Long = ???

    def loadText(asset: sgl.assets.TextAsset): Loader[Array[String]] = ???

    def loadBinary(asset: sgl.assets.RawImageAsset): Loader[Array[Byte]] = ???

    def openWebpage(uri: java.net.URI): Unit = ???

  }
  override val System: System = new TestSystem

}
