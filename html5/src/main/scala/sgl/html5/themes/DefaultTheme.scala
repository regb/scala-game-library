package sgl.html5
package themes

import org.scalajs.dom
import dom.html

/** The default theme for a website with a canvas game.
  *
  * The canvas will try to fit all available space, up to
  * the maxFrame, if there's more space available, it will just
  * put the canvas centered with the maxFrame or the optimalFrame
  * dimensions.
  *
  * This theme works well for a mobile game in portrait mode, as it should
  * ideally fill the entire space on the mobile, and it would be displayed
  * on a background on desktop, and still playable.
  *
  * The rest of the body, if visible, has a background color that can be
  * overriden as well.
  *
  * It's advisable to set the following meta tag to your html:
  *   <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0' />
  * This will disable the ability to zoom on mobile, although iOS doesn't seem
  * to respect it these days. There's a strong opinion out there that you
  * should never use this tag, however I don't think it applies to
  * mobile web game, and I would always set this behavior if possible.
  */
trait DefaultTheme extends Theme {

  /** Override the background color behind the canvas game. */
  val backgroundColor: String = "rgb(42,42,42)"

  /** Maximal supported (width, height) for the canvas (in CSS pixels).
    *
    * The canvas guarantees that its width and height will never be more than
    * this. It will try to use as much space as possible up to these.
    *
    * As long as the window width and height are smaller than the maxFrame, we
    * will set the canvas to the entire available space (window width and
    * height). This will create a canvas with the aspect ratio of the screen,
    * not of the specified maxFrame.
    *
    * If there's enough space in one dimension of the window but not on
    * the other, we will fill all the space on the dimension that has
    * less than the maxFrame space, and on the other we will fill as much
    * as possible while respecting the aspect ratio of optimalFrame (if set)
    * or of maxFrame otherwise. If optimalFrame is set with an extreme ratio,
    * it might push the other dimension to go over the maxFrame, in that
    * case we would crop it to the maxFrame dimension.
    *
    * If there's enough window space to cover both of these, we will set the
    * canvas to be exactly these dimensions, unless optimalFrame is set, in
    * which case we will use exactly optimalFrame dimensions.
    **/
  val maxFrame: (Int, Int)

  /** Set the optimal dimensions of the canvas (in CSS pixels).
    *
    * This is only used if the window width/height overflows
    * the maxFrame dimensions and thus we cannot fit the entire
    * screen. In that case, we would first set the canvas to
    * these optimal dimensions, and only if it is not defined
    * we would use the maxFrame dimensions.
    *
    * If set, it should always be smaller than the maxFrame
    * dimensions.
    */
  val optimalFrame: Option[(Int, Int)] = None

  override def init(canvas: html.Canvas): Unit = {
    require(optimalFrame.forall(p => p._1 <= maxFrame._1 && p._2 <= maxFrame._2))

    dom.document.body.style.backgroundColor = backgroundColor
    dom.document.body.style.margin = "0"
    dom.document.body.style.padding = "0"
    // We make sure that scroll bar is hidden because we always
    // try to fit the window in the available space.
    dom.document.body.style.overflow = "hidden"

    // Prevent highlight on click on canvas.
    dom.document.onselectstart = (e: dom.Event) => false

    canvas.style.margin = "0"
    canvas.style.padding = "0"
    canvas.style.display = "block"
    canvas.style.position = "absolute"

    setDimensions(canvas)
  }

  override def onResize(canvas: html.Canvas): Unit = {
    setDimensions(canvas)
  }

  /* 
   * we set the dimension in javascript, as we need to set the width/height
   * attribute of the canvas, and not just the CSS properties.  The pixel
   * coordinates in the canvas depend on the width/height attributes of the
   * canvas. Since we want to automatically resize the canvas to the full
   * screen on mobile (without stretching or black borders) we need some hooks
   * in javascript to automatically adapt the size to the screen.
   */
   private def setDimensions(canvas: html.Canvas): Unit = {
    /* 
     * We can use either window.innerWidth or document.body.clientWidth, the
     * latter returns the width ignoring the scroll bar, which is slightly more
     * correct, although we can just hide the scrollbar and end up with the
     * same behavior for both.
     */
    val windowWidth = dom.window.innerWidth.toInt
    val windowHeight = dom.window.innerHeight.toInt

    if(windowWidth < maxFrame._1 && windowHeight < maxFrame._2) {
      /* We are within the maxFrame, so we use the entire screen estate. */
      canvas.width = windowWidth
      canvas.height = windowHeight 
    } else if(windowWidth < maxFrame._1) {
      /* We can fill all the window width, but we restrict the height of the canvas. */
      canvas.width = windowWidth
      val ratio = windowWidth/optimalFrame.map(_._1.toDouble).getOrElse(maxFrame._1.toDouble)
      val scaledHeight = ratio*optimalFrame.map(_._2.toDouble).getOrElse(maxFrame._2.toDouble)
      canvas.height = scaledHeight.toInt min maxFrame._2
    } else if(windowHeight < maxFrame._2) {
      /* We can fill all the window height, but we restrict the width of the canvas. */
      canvas.height = windowHeight
      val ratio = windowHeight/optimalFrame.map(_._2.toDouble).getOrElse(maxFrame._2.toDouble)
      val scaledWidth = ratio*optimalFrame.map(_._1.toDouble).getOrElse(maxFrame._1.toDouble)
      canvas.width = scaledWidth.toInt min maxFrame._1
    } else {
      val (w, h) = optimalFrame.getOrElse(maxFrame)
      canvas.width = w
      canvas.height = h
    }

    val left: Int = (windowWidth - canvas.width)/2
    val top: Int = (windowHeight - canvas.height)/2
    if(left == 0)
      canvas.style.left = "0"
    else
      canvas.style.left = left + "px"
    if(top == 0)
      canvas.style.top = "0"
    else
      canvas.style.top = top + "px"
  }
}
