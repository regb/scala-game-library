package sgl
package html5

import java.net.URI

import org.scalajs.dom

trait Html5SystemProvider extends SystemProvider {

  override def exit(): Unit = {}


  //probably cleaner to return lazily and block only when iterator is called
  //class LazyTextResource(rawFile: dom.XMLHttpRequest) extends Iterator[String] = {

  //}
  //but the best would be to redefine these loading APIs to be async

  override def loadTextResource(path: String): Iterator[String] = {
    var res: Iterator[String] = null
    val rawFile = new dom.XMLHttpRequest()
    rawFile.open("GET", "static/" + path, false)
    rawFile.onreadystatechange = (event: dom.Event) => {
      if(rawFile.readyState == 4) {
        if(rawFile.status == 200 || rawFile.status == 0) {
          res = rawFile.responseText.split("\n").iterator
        }
      }
    }
    rawFile.send(null)
    res
  }

  override def openWebpage(uri: URI): Unit = {
    dom.window.open(uri.toString)
  }

  object Html5System extends System {

    case class StringPath(path: String) extends AbstractPath {
      override def / (filename: String): Path = StringPath(path + "/" + filename)
    }
    type Path = StringPath
    val root: Path = StringPath("")
  }
  val System = Html5System
}
