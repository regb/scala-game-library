package sgl
package html5

import java.net.URI

import org.scalajs.dom
import scala.scalajs.js
import js.typedarray.{ArrayBuffer, TypedArrayBuffer}

import sgl.util._

trait Html5SystemProvider extends SystemProvider with PartsResourcePathProvider {

  object Html5System extends System {

    override def exit(): Unit = {}

    override def currentTimeMillis: Long = js.Date.now.toLong

    // Note that there is no way to get nanosecond precision in Javascript, so we
    // have to do with microsecond granularity.
    override def nanoTime: Long = (dom.window.performance.now()*1000l*1000l).toLong

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
            val responseBuffer: ArrayBuffer = fileReq.response.asInstanceOf[ArrayBuffer]
            val bb: java.nio.ByteBuffer = TypedArrayBuffer.wrap(responseBuffer)
            val array: Array[Byte] = new Array(bb.remaining)
            bb.get(array)
            p.success(array)
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

}
