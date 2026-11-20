package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** Sizes the canvas backing store after the theme laid it out.
  *
  * To take advantage of high density screens, the backing store is scaled by
  * the device pixel ratio while the canvas keeps its CSS size. The game gets
  * more pixels to work with, and uses the ppi exported by the WindowProvider
  * to keep a similar physical appearance.
  */
private[html5] object CanvasLayout {

  def prepare(canvas: html.Canvas, theme: Theme): Unit = {
    val rect = canvas.getBoundingClientRect()
    val cssWidth = scala.math.max(1, rect.width.toInt)
    val cssHeight = scala.math.max(1, rect.height.toInt)

    if(!theme.preserveCanvasCssSize) {
      canvas.style.width = s"${cssWidth}px"
      canvas.style.height = s"${cssHeight}px"
    }

    canvas.width = scala.math.max(1, (dom.window.devicePixelRatio*cssWidth).toInt)
    canvas.height = scala.math.max(1, (dom.window.devicePixelRatio*cssHeight).toInt)
  }
}
