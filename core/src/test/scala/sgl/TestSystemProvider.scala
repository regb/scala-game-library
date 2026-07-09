package sgl

import java.net.URI

import scala.collection.mutable

import sgl.assets.{BinaryAsset, TextAsset}
import sgl.util.Loader

trait TestSystemProvider extends TestSystemNoResourcePathProvider with PartsResourcePathProvider {
  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
}

trait TestSystemNoResourcePathProvider extends SystemProvider {

  class TestSystem extends System {
    var exited: Boolean = false
    var wallClockMillis: Long = 0L
    var monotonicNanos: Long = 0L
    var lastOpenedWebpage: Option[URI] = None

    val textAssets: mutable.Map[TextAsset, Array[String]] = mutable.Map.empty
    val binaryAssets: mutable.Map[BinaryAsset, Array[Byte]] = mutable.Map.empty

    override def exit(): Unit = exited = true
    override def currentTimeMillis: Long = wallClockMillis
    override def nanoTime: Long = monotonicNanos

    override def loadText(asset: TextAsset): Loader[Array[String]] =
      textAssets.get(asset) match {
        case Some(lines) => Loader.successful(lines.clone())
        case None => Loader.failed(new NoSuchElementException(s"No test text asset registered: ${asset.resourceName}"))
      }

    override def loadBinary(asset: BinaryAsset): Loader[Array[Byte]] =
      binaryAssets.get(asset) match {
        case Some(bytes) => Loader.successful(bytes.clone())
        case None => Loader.failed(new NoSuchElementException(s"No test binary asset registered: ${asset.resourceName}"))
      }

    override def openWebpage(uri: URI): Unit = lastOpenedWebpage = Some(uri)
  }

  val testSystem: TestSystem = new TestSystem
  override val System: System = testSystem
}
