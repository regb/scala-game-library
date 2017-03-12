package sgl
package native

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

import scala.language.implicitConversions

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

    //TODO: can I also extend AnyVal ?
    case class StringPath(path: String) extends AbstractResourcePath {
      override def /(filename: String): ResourcePath = path + "/" + filename
    }
    type ResourcePath = String
    val ResourcesPrefix: ResourcePath = "resources"
  }
  val System = NativeSystem
  override implicit def wrapResourcePath(resourcePath: System.ResourcePath) = System.StringPath(resourcePath)

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  //implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
