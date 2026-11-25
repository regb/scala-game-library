package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** The canvas has a fixed size in CSS pixels.
  *
  * By default the theme also lays out the page: it styles the body and keeps
  * the canvas centered in it, overflowing (and requiring scrolling) when the
  * window is too small.
  *
  * Set `centerOnPage` to false to only size the canvas and leave the page to
  * its own HTML and CSS. The canvas then stays in the normal flow of the page,
  * so it can sit inside any container, and the theme changes nothing outside
  * the canvas element.
  */
trait FixedWindowTheme extends Theme {

  /** Override the background color behind the canvas game. Only used when `centerOnPage`. */
  val backgroundColor: String = "rgb(42,42,42)"

  /** The canvas always use these (width,height) (in CSS pixels). */
  val frameSize: (Int, Int)

  /** Whether the theme styles the body and centers the canvas on the page. */
  val centerOnPage: Boolean = true

  override def init(canvas: html.Canvas): Unit = {
    canvas.style.display = "block"
    if(centerOnPage) {
      dom.document.body.style.backgroundColor = backgroundColor
      dom.document.body.style.margin = "0"
      dom.document.body.style.padding = "0"

      // prevent highlight on click on canvas.
      dom.document.onselectstart = (_: dom.Event) => false

      canvas.style.margin = "0"
      canvas.style.padding = "0"
      canvas.style.position = "absolute"
    } else {
      CanvasLayout.setCssSize(canvas, frameSize._1, frameSize._2)
      canvas.style.setProperty("user-select", "none")
    }

    canvas.width = frameSize._1
    canvas.height = frameSize._2
    if(centerOnPage) setPosition(canvas)
  }

  override def onResize(canvas: html.Canvas): Unit = {
    // It's not great, but some other parts of the backend might change the width/height,
    // so we need to reset them here. Would be good to have something cleaner, but
    // not sure how.
    canvas.width = frameSize._1
    canvas.height = frameSize._2
    if(centerOnPage) setPosition(canvas)
  }

  /* Automatically center dynamically. */
  private def setPosition(canvas: html.Canvas): Unit = {
    /*
     * The root element's client size is the viewport without scroll bars. The
     * body's would be its content height, which is 0 when the canvas is its
     * only (absolutely positioned) child.
     */
    val windowWidth = dom.document.documentElement.clientWidth.toInt
    val windowHeight = dom.document.documentElement.clientHeight.toInt

    if(windowWidth < frameSize._1)
      canvas.style.left = "0"
    else {
      val left: Int = (windowWidth - canvas.width)/2
      canvas.style.left = s"${left}px"
    }

    if(windowHeight < frameSize._2)
      canvas.style.top = "0"
    else {
      val top: Int = (windowHeight - canvas.height)/2
      canvas.style.top = s"${top}px"
    }
  }
}
