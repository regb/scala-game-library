package sgl
package html5

import java.net.URI

trait Html5SystemProvider extends SystemProvider {

  override def exit(): Unit = ???

  override def loadTextResource(path: String): Iterator[String] = ???

  override def openWebpage(uri: URI): Unit = ???

}
