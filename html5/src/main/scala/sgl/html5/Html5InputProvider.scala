package sgl
package html5

import sgl.util._

import org.scalajs.dom
import dom.html


/* TODO: Explain that due to JS event loop, the input events are always handled outside
 * game loop update.
 * But it's important that game loop update also happen with regular the regular event loop.
 */
trait Html5InputProvider {
  this: Html5App with LoggingProvider =>

  import Input._

  private def mouseEventButton(e: dom.MouseEvent): MouseButtons.MouseButton = e.button match {
    case 0 => MouseButtons.Left
    case 1 => MouseButtons.Middle
    case 2 => MouseButtons.Right
    case _ =>  {
      //TODO: log unexpected code
      MouseButtons.Left
    }
  }

  /* 
   * The actual coordinates need to be translated to the canvas coordinates.
   * First we need to offset them by the canvas top-left coordinates, then we
   * need to adapt them to the actual canvas size.
   * The canvas is scaled up by devicePixelRatio, and thus its internal coordinates
   * system is finer than its size in CSS, but the click event will contain the
   * CSS coordinates, so we need to scale these up.
   */
  private def getCursorPosition(canvas: html.Canvas, clientX: Int, clientY: Int): (Int, Int) = {
    val rect = canvas.getBoundingClientRect()
    val x = ((clientX - rect.left)*dom.window.devicePixelRatio).toInt
    val y = ((clientY - rect.top)*dom.window.devicePixelRatio).toInt
    (x, y)
  }
  private def getCursorPosition(canvas: html.Canvas, e: dom.MouseEvent): (Int, Int) = {
    getCursorPosition(canvas, e.clientX.toInt, e.clientY.toInt)
  }

  // We track if the user has interacted with the game in any significant way (touch, click,
  // pressed keys?). This is because some browsers have a policy to not autoplay music, and
  // we need to wait for an actual user action before being able to play.
  private var hasUserInteracted = false
  private var actionsOnUserInteraction = new scala.collection.mutable.ListBuffer[() => Unit]
  // The function registered here will be called either immediately if the user already
  // interacted with the page, or on the initial interaction.
  def onInitialUserInteraction(f: () => Unit): Unit = {
    if(hasUserInteracted) f() else {
      actionsOnUserInteraction.append(() => f())
    }
  }
  private def triggerUserInteraction(): Unit = {
    if(!hasUserInteracted) {
      hasUserInteracted = true
      for(f <- actionsOnUserInteraction) {
        f()
      }
    }
  }

  def registerInputListeners(): Unit = {

    /*
     * Note that we don't listen to the click event and instead try to
     * synthesize it from down and up (in the scene graph library). There's a
     * chance that browsers might require some actions to take place only in
     * callbacks of click events (play sound, open links), but in practice it
     * seems they are ok as long as it happens during a reasonable event like a
     * mousedown/mouseup. It would be probably more robust to try to detect the
     * actual click event of the browser and somehow use it, but that would
     * require a bunch of refactoring as the aggregation of events happens in
     * the core module, and is thus platform agnostic.
     */
 
    this.htmlCanvas.addEventListener("mousedown", (e: dom.MouseEvent) => {
      triggerUserInteraction()
      val (x,y) = getCursorPosition(this.htmlCanvas, e)
      Input.inputProcessor.mouseDown(x, y, mouseEventButton(e))
    })
    this.htmlCanvas.addEventListener("mouseup", (e: dom.MouseEvent) => {
      triggerUserInteraction()
      val (x,y) = getCursorPosition(this.htmlCanvas, e)
      Input.inputProcessor.mouseUp(x, y, mouseEventButton(e))
    })
    this.htmlCanvas.addEventListener("mousemove", (e: dom.MouseEvent) => {
      val (x,y) = getCursorPosition(this.htmlCanvas, e)
      Input.inputProcessor.mouseMoved(x, y)
    })

    /*
     * for touch events, we use evt.preventDefault to
     * try to avoid the trigger of emulated mouse events that
     * mobile browsers tend to send, we want to capture only
     * the touch event, and not a duplicated mouse event since
     * we assume our game handles both correctly.
     *
     * It seems that preventDefault in the touchstart event will
     * also prevent the user from scrolling. This is fine, as long
     * as we assume scrolling is handled entirely by the game input
     * management. Since we only cancel a touchstart on the canvas
     * itself, the user could still scroll or zoom the rest of the
     * page if the canvas app is part of a bigger page, and otherwise
     * we are supposed to handle precisely the touch behaviour, so
     * either we scroll the canvas content itself, or make sure it always
     * fits the whole viewport.
     */

    this.htmlCanvas.addEventListener("touchstart", (e: dom.Event) => {
      triggerUserInteraction()
      val touchEvent = e.asInstanceOf[dom.raw.TouchEvent]
      touchEvent.preventDefault()
      touchEvent.stopPropagation()
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x,y) = getCursorPosition(this.htmlCanvas, touch.clientX.toInt, touch.clientY.toInt)
        val id = touch.identifier.toInt
        Input.inputProcessor.touchDown(x, y, id)
      }
    })
    this.htmlCanvas.addEventListener("touchend", (e: dom.Event) => {
      triggerUserInteraction()
      val touchEvent = e.asInstanceOf[dom.raw.TouchEvent]
      touchEvent.preventDefault()
      touchEvent.stopPropagation()
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x,y) = getCursorPosition(this.htmlCanvas, touch.clientX.toInt, touch.clientY.toInt)
        val id = touch.identifier.toInt
        Input.inputProcessor.touchUp(x, y, id)
      }
    })
    this.htmlCanvas.addEventListener("touchmove", (e: dom.Event) => {
      val touchEvent = e.asInstanceOf[dom.raw.TouchEvent]
      touchEvent.preventDefault()
      touchEvent.stopPropagation()
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x,y) = getCursorPosition(this.htmlCanvas, touch.clientX.toInt, touch.clientY.toInt)
        val id = touch.identifier.toInt
        Input.inputProcessor.touchMoved(x, y, id)
      }
    })

    dom.document.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      triggerUserInteraction()
      domEventToKey(e).foreach(key =>
        Input.inputProcessor.keyDown(key)
      )
    })
    dom.document.addEventListener("keyup", (e: dom.KeyboardEvent) => {
      triggerUserInteraction()
      domEventToKey(e).foreach(key =>
        Input.inputProcessor.keyUp(key)
      )
    })
  }

  //TODO: will need to make it more cross browser compatible
  private def domEventToKey(e: dom.KeyboardEvent): Option[Keys.Key] = e.keyCode match {
    case 32 => Some(Input.Keys.Space)

    case 37 => Some(Input.Keys.Left)
    case 38 => Some(Input.Keys.Up)
    case 39 => Some(Input.Keys.Right)
    case 40 => Some(Input.Keys.Down)

    case 48 => Some(Input.Keys.Num0)
    case 49 => Some(Input.Keys.Num1)
    case 50 => Some(Input.Keys.Num2)
    case 51 => Some(Input.Keys.Num3)
    case 52 => Some(Input.Keys.Num4)
    case 53 => Some(Input.Keys.Num5)
    case 54 => Some(Input.Keys.Num6)
    case 55 => Some(Input.Keys.Num7)
    case 56 => Some(Input.Keys.Num8)
    case 57 => Some(Input.Keys.Num9)

    case 65 => Some(Input.Keys.A)
    case 66 => Some(Input.Keys.B)
    case 67 => Some(Input.Keys.C)
    case 68 => Some(Input.Keys.D)
    case 69 => Some(Input.Keys.E)
    case 70 => Some(Input.Keys.F)
    case 71 => Some(Input.Keys.G)
    case 72 => Some(Input.Keys.H)
    case 73 => Some(Input.Keys.I)
    case 74 => Some(Input.Keys.J)
    case 75 => Some(Input.Keys.K)
    case 76 => Some(Input.Keys.L)
    case 77 => Some(Input.Keys.M)
    case 78 => Some(Input.Keys.N)
    case 79 => Some(Input.Keys.O)
    case 80 => Some(Input.Keys.P)
    case 81 => Some(Input.Keys.Q)
    case 82 => Some(Input.Keys.R)
    case 83 => Some(Input.Keys.S)
    case 84 => Some(Input.Keys.T)
    case 85 => Some(Input.Keys.U)
    case 86 => Some(Input.Keys.V)
    case 87 => Some(Input.Keys.W)
    case 88 => Some(Input.Keys.X)
    case 89 => Some(Input.Keys.Y)
    case 90 => Some(Input.Keys.Z)

    case _ => None
  }

}
