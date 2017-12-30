package sgl
package native

import sgl.util._

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

import scala.language.implicitConversions

trait NativeSystemProvider extends SystemProvider {

  object NativeSystem extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def millis(): Long = {
      // TODO: check that we can use java.lang.System ?
      java.lang.System.currentTimeMillis
    }

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

  case class StringPath(path: String) extends AbstractResourcePath {
    override def /(filename: String): ResourcePath = StringPath(path + "/" + filename)
  }
  type ResourcePath = StringPath
  val ResourcesPrefix: ResourcePath = StringPath("assets")

}
