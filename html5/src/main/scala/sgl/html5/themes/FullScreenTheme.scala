package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** The canvas is always using the full screen available.
  *
  * This probably makes the most sense for a native app using
  * a webview around the game. The other use case might be when
  * you want to handle everything in the canvas (with code that adapts
  * to the available space, and for serious desktop games with a
  * web mode).
  */
class FullScreenTheme extends Theme {

  override def init(canvas: html.Canvas): Unit = {
    dom.document.body.style.margin = "0"
    dom.document.body.style.padding = "0"
    dom.document.body.style.overflow = "hidden"

    // prevent highlight on click on canvas.
    dom.document.onselectstart = (e: dom.Event) => false

    canvas.style.margin = "0"
    canvas.style.padding = "0"
    canvas.style.display = "block"
    canvas.style.position = "absolute"
    canvas.style.left = "0"
    canvas.style.top = "0"

    canvas.width = dom.window.innerWidth.toInt
    canvas.height = dom.window.innerHeight.toInt
  }

  override def onResize(canvas: html.Canvas): Unit = {
    canvas.width = dom.window.innerWidth.toInt
    canvas.height = dom.window.innerHeight.toInt
  }

}
