package sgl
package native

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

trait NativeSystemProvider extends SystemProvider {

  override def exit(): Unit = {
    sys.exit()
  }

  override def loadTextResource(path: String): Iterator[String] = {

    ???
    //val is = getClass.getClassLoader.getResourceAsStream(path)
    //scala.io.Source.fromInputStream(is).getLines
  }

  override def openWebpage(uri: URI): Unit = {
    ()
    //val desktop = if(Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    //if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
    //  try {
    //    desktop.browse(uri);
    //  } catch {
    //    case (e: Exception) =>
    //      e.printStackTrace()
    //  }
    //}
  }

  object NativeSystem extends System {

    case class StringPath(path: String) extends AbstractResourcePath {
      //We don't use java.io.File.separator for the "/" separator, as
      //resource path must always use "/", even on Windows
      override def / (filename: String): ResourcePath =
        if(this == ResourcesPrefix) StringPath(filename) else StringPath(path + "/" + filename)
    }
    type ResourcePath = StringPath
    val ResourcesPrefix: ResourcePath = StringPath("")
  }
  val System = NativeSystem

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  //implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
