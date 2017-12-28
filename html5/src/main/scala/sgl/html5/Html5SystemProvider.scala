package sgl
package html5

import java.net.URI

import org.scalajs.dom

import sgl.util._

trait Html5SystemProvider extends SystemProvider {

  object Html5System extends System {

    override def exit(): Unit = {}

    //probably cleaner to return lazily and block only when iterator is called
    //class LazyTextResource(rawFile: dom.XMLHttpRequest) extends Iterator[String] = {

    //}
    //but the best would be to redefine these loading APIs to be async

    override def loadText(path: ResourcePath): Loader[Array[String]] = {
      val p = new DefaultLoader[Array[String]]()
      val rawFile = new dom.XMLHttpRequest()
      rawFile.open("GET", "static/" + path.path, true)
      rawFile.onreadystatechange = (event: dom.Event) => {
        if(rawFile.readyState == 4) {
          if(rawFile.status == 200 || rawFile.status == 0) {
            p.success(rawFile.responseText.split("\n").toArray)
          } else {
            p.failure(new RuntimeException("file: " + path + " failed to load"))
          }
        }
      }
      rawFile.send(null)
      p.loader
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = {
      val p = new DefaultLoader[Array[Byte]]()
      val fileReq = new dom.XMLHttpRequest()
      fileReq.open("GET", "static/" + path.path, true)
      fileReq.responseType = "arraybuffer"
      fileReq.onreadystatechange = (event: dom.Event) => {
        if(fileReq.readyState == 4) {
          if(fileReq.status == 200 || fileReq.status == 0) {
            p.success(fileReq.response.toArray)
          } else {
            p.failure(new RuntimeException("file: " + path + " failed to load"))
          }
        }
      }
      fileReq.send(null)
      p.loader
    }

    override def openWebpage(uri: URI): Unit = {
      dom.window.open(uri.toString)
    }

  }
  val System = Html5System

  case class StringPath(path: String) extends AbstractResourcePath {
    override def / (filename: String): ResourcePath = StringPath(path + "/" + filename)
  }
  type ResourcePath = StringPath
  val ResourcesPrefix: ResourcePath = StringPath("")

}
