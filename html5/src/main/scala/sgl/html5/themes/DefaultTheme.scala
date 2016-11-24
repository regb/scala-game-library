package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** The default theme for a website with a canvas game
  *
  * The website should be mobile friendly, adapting full screen
  * to the device. On the desktop,
  * we set the canvas dimensions to match the maxFrame property, and
  * centered in the middle of the page. If there is not enough screen
  * estate, but we are still not on mobile, we will maintain the aspect ratio
  * while stretching in one direction as much as possible.
  *
  * The rest of the body, if visible, has a background color that can be
  * overriden as well.
  */
class DefaultTheme extends Theme {

  /** Override the background color behind the canvas game */
  val backgroundColor: String = "rgb(42,42,42)"

  /** Use these dimensions for the canvas if enough screen estate */
  val maxFrame: (Int, Int) = (480, 720)


  override def init(canvas: html.Canvas): Unit = {
    dom.document.body.style.backgroundColor = backgroundColor
    dom.document.body.style.margin = "0"
    dom.document.body.style.padding = "0"

    //prevent highlight on click on canvas
    dom.document.onselectstart = (e: dom.Event) => false

    canvas.style.margin = "0"
    canvas.style.padding = "0"
    canvas.style.display = "block"
    canvas.style.position = "absolute"


    dom.window.onresize = (event: dom.Event) => {
      setDimensions(canvas)
    }
    setDimensions(canvas)

  }


  //we set the dimension in javascript, as we need to set the
  //width/height attribute of the canvas, and not just the CSS properties.
  //The pixel coordinates in the canvas depend on the width/height attributes
  //of the canvas. Since we need want to automatically resize the canvas to the full
  //screen on mobile (without stretching or black borders) we need some hooks
  //in javascript to automatically adapt the size to the screen.
  private def setDimensions(canvas: html.Canvas): Unit = {
    val windowWidth = dom.window.innerWidth.toInt
    val windowHeight = dom.window.innerHeight.toInt

    if(windowWidth < 480) { //"mobile" cut-off
      canvas.width = windowWidth
      canvas.height = windowHeight 
      canvas.style.left = "0"
      canvas.style.top = "0"
    } else {
      if(windowHeight < maxFrame._2) {
        val ratio = windowHeight.toDouble / maxFrame._2.toDouble
        canvas.height = windowHeight
        canvas.width = (ratio*maxFrame._1).toInt
      } else {
        canvas.width = maxFrame._1
        canvas.height = maxFrame._2
      }

      val left: Int = (windowWidth - canvas.width)/2
      val top: Int = (windowHeight - canvas.height)/2
      canvas.style.left = left + "px"
      canvas.style.top = top + "px"
    }

  }
}
