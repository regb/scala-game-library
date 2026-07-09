package sgl
package html5

import sgl.util.LoggingProvider

import org.scalajs.dom
import dom.html

import scala.collection.mutable

/** Controls where the HTML5 backend listens for keyboard events.
  *
  * The default keeps the historical standalone-game behavior: keyboard events
  * are captured globally by the page/window. Embedded games can opt into
  * CanvasFocused so surrounding UI, such as forms or chat inputs, owns the
  * keyboard unless the game canvas is focused.
  */
sealed trait Html5KeyboardCaptureMode

object Html5KeyboardCaptureMode {
  case object Global extends Html5KeyboardCaptureMode
  case object CanvasFocused extends Html5KeyboardCaptureMode
}

/* TODO: Explain that due to JS event loop, the input events are always handled outside
 * game loop update.
 * But it's important that game loop update also happen with regular the regular event loop.
 */
trait Html5InputProvider {
  this: Html5SystemProvider with LoggingProvider =>

  def htmlCanvas: html.Canvas

  /** Controls where keyboard events are listened for. */
  protected val Html5KeyboardCapture: Html5KeyboardCaptureMode = Html5KeyboardCaptureMode.Global

  /** Whether keyboard events captured by the game should prevent browser defaults. */
  protected val Html5KeyboardPreventDefault: Boolean = true

  /** Whether mouse events captured by the game should prevent browser defaults. */
  protected val Html5MousePreventDefault: Boolean = true

  /** Whether wheel events captured by the game should prevent browser defaults. */
  protected val Html5WheelPreventDefault: Boolean = true

  /** Whether touch events captured by the game should prevent browser defaults. */
  protected val Html5TouchPreventDefault: Boolean = true

  /** Whether touch events captured by the game should stop propagation. */
  protected val Html5TouchStopPropagation: Boolean = true

  protected def Html5GlobalKeyboardTarget: dom.EventTarget = dom.document
  protected def Html5ReleaseMouseOnLeave: Boolean = false
  protected def Html5ReleaseMouseOnWindowMouseUp: Boolean = false
  protected def onHtml5WindowBlur(): Unit = ()
  protected def onHtml5WindowFocus(): Unit = ()

  // We track if the user has interacted with the game in any significant way (touch, click,
  // pressed keys?). This is because some browsers have a policy to not autoplay music, and
  // we need to wait for an actual user action before being able to play.
  private var hasUserInteracted = false
  private val actionsOnUserInteraction = new scala.collection.mutable.ListBuffer[() => Unit]
  private var removeInputListeners: () => Unit = () => ()

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
    removeInputListeners()
    removeInputListeners = Html5DomInputAdapter.register(
      canvas = this.htmlCanvas,
      keyboardCapture = Html5KeyboardCapture,
      onUserInteraction = () => triggerUserInteraction(),
      globalKeyboardTarget = Html5GlobalKeyboardTarget,
      keyboardPreventDefault = Html5KeyboardPreventDefault,
      mousePreventDefault = Html5MousePreventDefault,
      wheelPreventDefault = Html5WheelPreventDefault,
      touchPreventDefault = Html5TouchPreventDefault,
      touchStopPropagation = Html5TouchStopPropagation,
      releaseMouseOnLeave = Html5ReleaseMouseOnLeave,
      releaseMouseOnWindowMouseUp = Html5ReleaseMouseOnWindowMouseUp,
      onWindowBlur = () => onHtml5WindowBlur(),
      onWindowFocus = () => onHtml5WindowFocus(),
    )
  }

  protected def disposeHtml5Input(): Unit = {
    removeInputListeners()
    removeInputListeners = () => ()
  }
}

private[html5] object Html5DomInputAdapter {

  def register(
    canvas: html.Canvas,
    keyboardCapture: Html5KeyboardCaptureMode,
    onUserInteraction: () => Unit = () => (),
    globalKeyboardTarget: dom.EventTarget = dom.document,
    keyboardPreventDefault: Boolean = true,
    mousePreventDefault: Boolean = true,
    wheelPreventDefault: Boolean = true,
    touchPreventDefault: Boolean = true,
    touchStopPropagation: Boolean = true,
    releaseMouseOnLeave: Boolean = false,
    releaseMouseOnWindowMouseUp: Boolean = false,
    onWindowBlur: () => Unit = () => (),
    onWindowFocus: () => Unit = () => (),
  ): () => Unit = {
    val listeners = mutable.ArrayBuffer.empty[(dom.EventTarget, String, dom.Event => Unit)]
    def listen(target: dom.EventTarget, eventType: String)(handler: dom.Event => Unit): Unit = {
      target.addEventListener(eventType, handler)
      listeners += ((target, eventType, handler))
    }

    val captureKeyboardOnCanvas = keyboardCapture == Html5KeyboardCaptureMode.CanvasFocused

    val activeMouseButtons = mutable.Set.empty[Input.MouseButtons.MouseButton]
    var lastMouseX = 0
    var lastMouseY = 0

    def releaseMouse(): Unit = {
      val buttons = activeMouseButtons.toVector
      activeMouseButtons.clear()
      buttons.foreach(button => Input.inputProcessor.mouseUp(lastMouseX, lastMouseY, button))
    }

    def focusCanvas(): Unit = {
      if(captureKeyboardOnCanvas) canvas.focus()
    }

    if(captureKeyboardOnCanvas && canvas.tabIndex < 0) canvas.tabIndex = 0

    val pressedKeys = mutable.Set.empty[Input.Keys.Key]
    def releaseKeyboard(): Unit = {
      val keysToRelease = pressedKeys.toList
      pressedKeys.clear()
      keysToRelease.foreach(Input.inputProcessor.keyUp)
    }

    def consumeMouseEvent(e: dom.MouseEvent): Unit = {
      if(mousePreventDefault) e.preventDefault()
    }

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
    listen(canvas, "mousemove") { event =>
      val e = event.asInstanceOf[dom.MouseEvent]
      consumeMouseEvent(e)
      val (x, y) = cursorPosition(canvas, e)
      lastMouseX = x
      lastMouseY = y
      Input.inputProcessor.mouseMoved(x, y)
    }
    listen(canvas, "mousedown") { event =>
      val e = event.asInstanceOf[dom.MouseEvent]
      consumeMouseEvent(e)
      onUserInteraction()
      focusCanvas()
      val (x, y) = cursorPosition(canvas, e)
      lastMouseX = x
      lastMouseY = y
      val button = mouseButton(e.button.toInt)
      activeMouseButtons += button
      Input.inputProcessor.mouseDown(x, y, button)
    }
    listen(canvas, "mouseup") { event =>
      val e = event.asInstanceOf[dom.MouseEvent]
      consumeMouseEvent(e)
      onUserInteraction()
      val (x, y) = cursorPosition(canvas, e)
      lastMouseX = x
      lastMouseY = y
      val button = mouseButton(e.button.toInt)
      if(activeMouseButtons.remove(button)) Input.inputProcessor.mouseUp(x, y, button)
    }
    if(releaseMouseOnLeave) {
      listen(canvas, "mouseleave") { _ => releaseMouse() }
    }
    listen(canvas, "wheel") { event =>
      val e = event.asInstanceOf[dom.WheelEvent]
      if(wheelPreventDefault) e.preventDefault()
      Input.inputProcessor.mouseScrolled(e.deltaY.toInt)
    }
    if(releaseMouseOnWindowMouseUp) {
      listen(dom.window, "mouseup") { event =>
        val e = event.asInstanceOf[dom.MouseEvent]
        val button = mouseButton(e.button.toInt)
        if(activeMouseButtons.remove(button)) Input.inputProcessor.mouseUp(lastMouseX, lastMouseY, button)
      }
    }

    val touchPointers = new mutable.HashMap[Int, Int]
    def sglTouchPointer(touchIdentifier: Int): Int = {
      touchPointers.getOrElseUpdate(touchIdentifier, {
        val usedPointers = touchPointers.values.toSet
        (0 until MaxTouchPointers).find(!usedPointers.contains(_)).getOrElse(0)
      })
    }
    def releaseSglTouchPointer(touchIdentifier: Int): Unit = {
      touchPointers.remove(touchIdentifier)
    }

    def consumeTouchEvent(touchEvent: dom.TouchEvent): Unit = {
      if(touchPreventDefault) touchEvent.preventDefault()
      if(touchStopPropagation) touchEvent.stopPropagation()
    }

    /*
     * For touch events, preventDefault is commonly used to avoid emulated mouse
     * events that mobile browsers tend to send after touch input. We want to
     * capture the touch event without also receiving a duplicated mouse event,
     * since SGL assumes the game handles both correctly.
     *
     * preventDefault on touchstart also prevents browser scrolling. This is the
     * desired default for standalone canvas games, but it is configurable because
     * embedded games may want the rest of the page to keep normal scroll/zoom
     * behavior. Since touch listeners are registered on the canvas itself, the
     * impact is local to touches that start on the game canvas.
     */
    listen(canvas, "touchstart") { e =>
      onUserInteraction()
      focusCanvas()
      val touchEvent = e.asInstanceOf[dom.TouchEvent]
      consumeTouchEvent(touchEvent)
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x, y) = cursorPosition(canvas, touch.clientX.toInt, touch.clientY.toInt)
        val id = sglTouchPointer(touch.identifier.toInt)
        Input.inputProcessor.touchDown(x, y, id)
      }
    }
    listen(canvas, "touchend") { e =>
      onUserInteraction()
      val touchEvent = e.asInstanceOf[dom.TouchEvent]
      consumeTouchEvent(touchEvent)
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x, y) = cursorPosition(canvas, touch.clientX.toInt, touch.clientY.toInt)
        val touchIdentifier = touch.identifier.toInt
        val id = sglTouchPointer(touchIdentifier)
        Input.inputProcessor.touchUp(x, y, id)
        releaseSglTouchPointer(touchIdentifier)
      }
    }
    listen(canvas, "touchmove") { e =>
      val touchEvent = e.asInstanceOf[dom.TouchEvent]
      consumeTouchEvent(touchEvent)
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x, y) = cursorPosition(canvas, touch.clientX.toInt, touch.clientY.toInt)
        val id = sglTouchPointer(touch.identifier.toInt)
        Input.inputProcessor.touchMoved(x, y, id)
      }
    }
    listen(canvas, "touchcancel") { e =>
      val touchEvent = e.asInstanceOf[dom.TouchEvent]
      consumeTouchEvent(touchEvent)
      val touches = touchEvent.changedTouches

      var i = 0
      while(i < touches.length) {
        val touch = touches(i)
        i += 1
        val (x, y) = cursorPosition(canvas, touch.clientX.toInt, touch.clientY.toInt)
        val touchIdentifier = touch.identifier.toInt
        touchPointers.get(touchIdentifier).foreach { id =>
          Input.inputProcessor.touchUp(x, y, id)
          releaseSglTouchPointer(touchIdentifier)
        }
      }
    }

    val keyboardTarget: dom.EventTarget = if(captureKeyboardOnCanvas) canvas else globalKeyboardTarget

    listen(keyboardTarget, "keydown") { event =>
      val e = event.asInstanceOf[dom.KeyboardEvent]
      key(e).foreach { k =>
        if(keyboardPreventDefault) e.preventDefault()
        onUserInteraction()
        if(!pressedKeys(k)) {
          pressedKeys += k
          Input.inputProcessor.keyDown(k)
        }
      }
    }
    listen(keyboardTarget, "keyup") { event =>
      val e = event.asInstanceOf[dom.KeyboardEvent]
      key(e).foreach { k =>
        if(keyboardPreventDefault) e.preventDefault()
        onUserInteraction()
        if(pressedKeys.remove(k)) {
          Input.inputProcessor.keyUp(k)
        }
      }
    }

    listen(dom.window, "blur") { _ =>
      releaseMouse()
      releaseKeyboard()
      onWindowBlur()
    }
    if(captureKeyboardOnCanvas) {
      listen(canvas, "blur") { _ => releaseKeyboard() }
    }
    listen(dom.window, "focus") { _ => onWindowFocus() }

    () => {
      releaseMouse()
      releaseKeyboard()
      touchPointers.clear()
      listeners.foreach { case (target, eventType, handler) => target.removeEventListener(eventType, handler) }
      listeners.clear()
    }
  }

  private val MaxTouchPointers = 10

  /*
   * The actual coordinates need to be translated to canvas coordinates.
   * First offset them by the canvas top-left coordinates, then adapt them to
   * the canvas backing-store size. The event contains CSS coordinates, while the
   * canvas internal size can differ from its CSS size, especially on high-DPI
   * displays.
   */
  private def cursorPosition(canvas: html.Canvas, e: dom.MouseEvent): (Int, Int) =
    cursorPosition(canvas, e.clientX.toInt, e.clientY.toInt)

  private def cursorPosition(canvas: html.Canvas, clientX: Int, clientY: Int): (Int, Int) = {
    val rect = canvas.getBoundingClientRect()
    val scaleX = canvas.width.toDouble / rect.width
    val scaleY = canvas.height.toDouble / rect.height
    val x = ((clientX - rect.left) * scaleX).toInt
    val y = ((clientY - rect.top) * scaleY).toInt
    (x, y)
  }

  private def mouseButton(button: Int): Input.MouseButtons.MouseButton = button match {
    case 1 => Input.MouseButtons.Middle
    case 2 => Input.MouseButtons.Right
    // TODO: log unexpected button codes.
    case _ => Input.MouseButtons.Left
  }

  // TODO: will need to make it more cross-browser compatible.
  private def key(e: dom.KeyboardEvent): Option[Input.Keys.Key] = e.keyCode match {
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
