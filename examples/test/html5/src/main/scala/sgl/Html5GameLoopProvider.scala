package sgl
package html5

import util._

import org.scalajs.dom
import dom.html

trait Html5GameLoopProvider extends GameLoopProvider {
  self: Html5GraphicsProvider with GameStateComponent with LoggingProvider =>

  abstract override def resume(): Unit = {
    dom.window.setInterval(() => println("boo"), 1000)
    super.resume()
  }

  abstract override def pause(): Unit = {
    super.pause()
  }

}
