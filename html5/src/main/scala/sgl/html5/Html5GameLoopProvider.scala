package sgl
package html5

import util._

import scala.scalajs.js
import org.scalajs.dom
import dom.html

trait Html5GameLoopProvider extends GameLoopProvider {
  self: Html5GraphicsProvider with GameStateComponent with LoggingProvider =>

  abstract override def resume(): Unit = {
    super.resume()

    var lastTime: Long = js.Date.now.toLong
    dom.window.setInterval(() => {

      val now = js.Date.now.toLong
      val dt: Long = now - lastTime
      lastTime = now

      val canvas = getScreenCanvas
      gameLoopStep(dt, canvas)
      releaseScreenCanvas(getScreenCanvas)

    }, 40)
  }

  abstract override def pause(): Unit = {
    super.pause()
  }

}
