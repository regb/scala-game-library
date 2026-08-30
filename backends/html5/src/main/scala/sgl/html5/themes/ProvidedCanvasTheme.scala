package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** Uses the canvas element as provided by the hosting page.
  *
  * This theme does not change body styles, canvas position, margins, or CSS
  * dimensions. It only reads the canvas CSS layout size and updates the canvas
  * backing-store size so the game renders into the element supplied by the page.
  */
class ProvidedCanvasTheme extends Theme {

  override def init(canvas: html.Canvas): Unit = {
    updateCanvasSizeFromLayout(canvas)
  }

  override def onResize(canvas: html.Canvas): Unit = {
    updateCanvasSizeFromLayout(canvas)
  }

  override def preserveCanvasCssSize: Boolean = true

  private def updateCanvasSizeFromLayout(canvas: html.Canvas): Unit = {
    val rect = canvas.getBoundingClientRect()
    val width = math.max(1, rect.width.toInt)
    val height = math.max(1, rect.height.toInt)
    canvas.width = width
    canvas.height = height
  }
}
