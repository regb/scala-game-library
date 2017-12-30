package sgl
package awt

import sgl.util._

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

trait AWTSystemProvider extends SystemProvider {

  object AWT5System extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def millis(): Long = {
      java.lang.System.currentTimeMillis
    }

    override def loadText(path: ResourcePath): Loader[Array[String]] = {
      FutureLoader {
        val is = getClass.getClassLoader.getResourceAsStream(path.path)
        scala.io.Source.fromInputStream(is).getLines.toArray
      }
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = {
      FutureLoader {
        val is = getClass.getClassLoader.getResourceAsStream(path.path)
        val bis = new java.io.BufferedInputStream(is)
        val bytes = new scala.collection.mutable.ListBuffer[Byte]
        var b: Int = 0
        while({ b = bis.read; b != -1}) {
          bytes.append(b.toByte)
        }
        bytes.toArray
      }
    }

    override def openWebpage(uri: URI): Unit = {
      val desktop = if(Desktop.isDesktopSupported()) Desktop.getDesktop() else null
      if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(uri);
        } catch {
          case (e: Exception) =>
            e.printStackTrace()
        }
      }
    }

  }
  val System = AWT5System

  case class StringPath(path: String) extends AbstractResourcePath {
    //We don't use java.io.File.separator for the "/" separator, as
    //resource path must always use "/", even on Windows
    override def / (filename: String): ResourcePath =
      if(this == ResourcesPrefix) StringPath(filename) else StringPath(path + "/" + filename)
  }
  type ResourcePath = StringPath

  override val ResourcesPrefix: ResourcePath = StringPath("")

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
