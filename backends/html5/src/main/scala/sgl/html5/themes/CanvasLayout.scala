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
    val content = ContentBox.of(canvas)
    val cssWidth = scala.math.max(1, content.width.toInt)
    val cssHeight = scala.math.max(1, content.height.toInt)

    if(!theme.preserveCanvasCssSize) setCssSize(canvas, cssWidth, cssHeight)

    canvas.width = scala.math.max(1, (dom.window.devicePixelRatio*cssWidth).toInt)
    canvas.height = scala.math.max(1, (dom.window.devicePixelRatio*cssHeight).toInt)
  }

  /** Sets the CSS size of what is drawn, whatever border or padding the page
    * puts around it.
    */
  def setCssSize(canvas: html.Canvas, width: Int, height: Int): Unit = {
    canvas.style.setProperty("box-sizing", "content-box")
    canvas.style.width = s"${width}px"
    canvas.style.height = s"${height}px"
  }
}

/** The part of a canvas that is drawn into, without its border and padding, in
  * CSS pixels. `left` and `top` are relative to its outer corner. CSS transforms
  * are not applied: they change how the canvas is shown, not its size.
  */
private[html5] final case class ContentBox(left: Double, top: Double, width: Double, height: Double)

private[html5] object ContentBox {
  def of(canvas: html.Canvas): ContentBox = {
    val style = dom.window.getComputedStyle(canvas)
    // A computed padding is always in pixels.
    def pixels(value: String): Double = value.stripSuffix("px").toDoubleOption.getOrElse(0.0)
    val (left, right) = (pixels(style.paddingLeft), pixels(style.paddingRight))
    val (top, bottom) = (pixels(style.paddingTop), pixels(style.paddingBottom))
    ContentBox(
      left = canvas.clientLeft + left,
      top = canvas.clientTop + top,
      width = canvas.clientWidth - left - right,
      height = canvas.clientHeight - top - bottom
    )
  }
}
