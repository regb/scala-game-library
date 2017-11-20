package sgl
package awt

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

trait AWTSystemProvider extends SystemProvider {

  object AWT5System extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def loadTextResource(path: String): Iterator[String] = {
      val is = getClass.getClassLoader.getResourceAsStream(path)
      scala.io.Source.fromInputStream(is).getLines
    }
    override def loadText(path: ResourcePath): Iterator[String] = {
      val is = getClass.getClassLoader.getResourceAsStream(path.path)
      scala.io.Source.fromInputStream(is).getLines
    }

    override def loadBinary(path: ResourcePath): Array[Byte] = {
      val is = getClass.getClassLoader.getResourceAsStream(path.path)
      val bis = new java.io.BufferedInputStream(is)
      val bytes = new scala.collection.mutable.ListBuffer[Byte]
      var b: Int = 0
      while({ b = bis.read; b != -1}) {
        bytes.append(b.toByte)
      }
      bytes.toArray
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
