package sgl
package html5

import java.net.URI

import org.scalajs.dom

trait Html5SystemProvider extends SystemProvider {

  override def exit(): Unit = {}

  override def loadTextResource(path: String): Iterator[String] = ???

  override def openWebpage(uri: URI): Unit = {
    dom.window.open(uri.toString)
  }

}
