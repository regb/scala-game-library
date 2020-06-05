package sgl
package native

import sgl.util._

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

import scala.language.implicitConversions

trait NativeSystemProvider extends SystemProvider with PartsResourcePathProvider {

  object NativeSystem extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    override def loadText(path: ResourcePath): Loader[Array[String]] = {
      ???
      //val is = getClass.getClassLoader.getResourceAsStream(path)
      //scala.io.Source.fromInputStream(is).getLines
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = {
      ???
    }

    override def openWebpage(uri: URI): Unit = {
      ???
    }
  }

  override val System = NativeSystem

  // TODO: This is not really a root as we start with a first part ("assets"). We
  // should instead add the assets prefix at the time when we convert the parts to
  // a path. For now, it's a fine hack to get something working though.
  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector("assets"))
  // TODO: Add support for multi dpi in loadImage (so do not always use drawable-mdpi).
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector("assets", "drawable-mdpi"))

}
