package sgl

/*
 * The design of the input handling abstraction went through a few
 * unsatisfying trials. The original design was to have GameScreen
 * implementing a processInputs function, with an InputBUffer being
 * passed with the different inputs since the last update.
 *
 * There were several issues with that attempt. First, the distinction
 * between events (a key down/up/pressed) and state (is the key up or down)
 * was a bit confusing. An event is only fired for one frame, then disappear,
 * while the state can last several frames. Events need to be captured
 * right on the frame it happens.
 * Additionally, we need a way to check if an event has been consumed
 * by some entity. This would be typically used for HUD on top of game, if
 * the HUD intercept a click event, the underlying game screen should ignore
 * the event.
 *
 * The current design goal is to clearly separate events from state. State
 * can be provided as a global, always available, data structure. So it
 * should be provided directly in the InputProvider component, and could
 * be queried at any time during update, since there should not be any
 * notion of who consumed the state first (unlike events).
 *
 *
 */

/** Provider for input handling */
trait InputProvider {

  val inputBuffer: InputBuffer = new InputBuffer


  object Input {
    import scala.collection.mutable.Queue

    private val eventQueue: Queue[InputEvent] = new Queue[InputEvent]()

    def newEvent(event: InputEvent): Unit = eventQueue.enqueue(event)

    /** Poll for the oldest non processed event.
      *
      * check if any events is present, and return an Option of an event.
      * The event is removed from the event queue.
      */
    def pollEvent(): Option[InputEvent] = if(eventQueue.isEmpty) None else Some(eventQueue.dequeue())

    //TODO: with an Event based architecture with immutable case
    //      classes, one concern is going to be garbage collecting
    //      the events. Hopefully, not too many events happen (since
    //      they are still player generated) and the garbage collection
    //      is not a big deal. We'll figure that out later anyway, better
    //      start with some clean interface.
    //      In any case, one solution could be to make the events mutable and
    //      automatically collect them for each tick, and then re-use them.
    sealed trait InputEvent
    case class KeyDownEvent(key: Keys.Key) extends InputEvent
    case class KeyUpEvent(key: Keys.Key) extends InputEvent

    //mouse events, no Dragged as it can be derived as a combination of Moved and Down
    sealed trait MouseInputEvent extends InputEvent
    case class MouseMovedEvent(x: Int, y: Int) extends MouseInputEvent
    case class MouseDownEvent(x: Int, y: Int, mouseButton: MouseButtons.MouseButton) extends MouseInputEvent
    case class MouseUpEvent(x: Int, y: Int, mouseButton: MouseButtons.MouseButton) extends MouseInputEvent
    //middle button scrolled, amount +/- depending on direction
    case class MouseScrolledEvent(amount: Int) extends MouseInputEvent

    //touch events, no Moved (as it would indicate a position, without a touch). The pointer is
    //the id of the pointer that is touching, to support multi-touch
    sealed trait TouchInputEvent extends InputEvent
    case class TouchDraggedEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchDownEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchUpEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent

    object MouseButtons {
      sealed trait MouseButton
      case object Left extends MouseButton
      case object Right extends MouseButton
      case object Middle extends MouseButton
    }

    object Keys {
      /*
       * One question is what is a key exactly. Could be only keyboard,
       * or could also be buttons from gamepad like Start/Select as well.
       */

      sealed trait Key

      case object A extends Key
      case object B extends Key
      case object C extends Key
      case object D extends Key
      case object E extends Key
      case object F extends Key
      case object G extends Key
      case object H extends Key
      case object I extends Key
      case object J extends Key
      case object K extends Key
      case object L extends Key
      case object M extends Key
      case object N extends Key
      case object O extends Key
      case object P extends Key
      case object Q extends Key
      case object R extends Key
      case object S extends Key
      case object T extends Key
      case object U extends Key
      case object V extends Key
      case object W extends Key
      case object X extends Key
      case object Y extends Key
      case object Z extends Key

      case object Space extends Key

      case object Left extends Key
      case object Up extends Key
      case object Right extends Key
      case object Down extends Key

      /*
       * Maybe we should have a Button type for those
       */
      case object ButtonStart extends Key
      case object ButtonSelect extends Key
    }

  }

  /** Provide current state of system Inputs
    *
    * This is an abstraction on top of the inputs provided by the
    * system. It's a global (to the cake) object as we consider inputs
    * are unique (you don't need to handle separate input providers) and
    * should be accessible to any part of the system.
    *
    * This is a superset of possible inputs, including
    * touch screen based (typically for mobile), mouse based, keyboards, etc.
    * It's mutable, but (ideally) should only be mutated by the input provider
    * backend implementation (TODO: try to enforce it with visibility declarations)
    * and is always up to date during a call to update.
    *
    * It exposes distinctly mouse and touch state, so if one
    * queries the state of the mouse like if it's down and its position, 
    * and the underlying platform is a touch based without a mouse, the
    * state of the mouse will not pretend to be a touch interface.
    *
    * The above design is to make sure to have perfect control, and to
    * design the best possible interface on each platform. To simplify
    * cross-platform programming, the Inputs also provides a PointingDevice
    * abstraction, which will expose any pointing-based input (mouse or touch)
    * as the same thing.
    *
    * Most state is expressed with the term "*Pressed". Pressed might be
    * a bit confusing since in Java we usually have down, up, and pressed
    * events, with pressed being fired only when up happens. But here, pressed
    * means the state is currently pressed (or down). A button/key can
    * be in a pressed state for many frames. Pressed was chosen as it sounded
    * better with some terms such as Buttons.leftPressed (versus leftDown, which
    * sounds like a direction).
    */
  object Inputs {
    /* TODO: I'm not sure this should really be the lowest level
     * abstraction on Inputs (along with events). Maybe it makes
     * more sense to only expose events, and have a separate module
     * processing events to maintain the Inputs state up-to-date?
     * That would mean, an InputProvider backend would not have to
     * worry about maintaining the current state, and we would just
     * update it automatically by some component handled in the main loop.
     * One question then is, if that component registers to events the
     * same way any game screen could do, we need to be pretty sure the
     * component has a chance to handle the events first, so that the
     * game screens then see the correct version of the Inputs state.
     */

    /** System-specific buttons
      *
      * Some buttons are left/right/middle mouse buttons,
      * back/menu button on Android, could also be controller
      * button from a gamepad. Keyboard keys are not buttons
      *
      * Note that, as for other states, this will not masquerade
      * stuff. So a leftPressed in only a mouse left button click,
      * and we won't set it if a single touch on the screen is for
      * example happening.
      *
      * Also, the fact that left is pressed means that the mouse pressed
      * state should be currently set to some coordinates.
      */
    object Buttons {
      var leftPressed: Boolean = false
      var middlePressed: Boolean = false
      var rightPressed: Boolean = false
      var backPressed: Boolean = false
      var menuPressed: Boolean = false
    }

    object Touch {
      //TODO: expose multi-touch ?
      var pressed: Option[(Int, Int)] = None
    }

    object Mouse {
      var position: Option[(Int, Int)] = None

      def pressed: Option[(Int, Int)] = leftPressed
      def leftPressed: Option[(Int, Int)] = if(Buttons.leftPressed) position else None
      def rightPressed: Option[(Int, Int)] = if(Buttons.rightPressed) position else None
    }

    object Keyboard {
      var left: Boolean = false
      var right: Boolean = false
      var up: Boolean = false
      var down: Boolean = false

      var w: Boolean = false
      var x: Boolean = false
    }

    /** abstraction over mouse, touch screen, stylus, and any other pointing device */
    object PointingDevice {
      /* TODO: probably need to have multiple pointing devices (like mouse + stylus, or multi-touch */

      def pressed: Option[(Int, Int)] = Mouse.leftPressed.orElse(Touch.pressed)
    }

  }

}

//object Events {
//  /*
//   * Touch events
//   */
//  var touchPoint: Option[(Int, Int)] = None
//  var touchScrollVector: Option[(Float, Float)] = None
//
//  //set in the frame where the touch is first down, and then up
//  var touchDown: Option[(Int, Int)] = None
//  var touchUp: Option[(Int, Int)] = None
//
//  var mouseClick: Option[(Int, Int)] = None
//  var mouseDown: Option[(Int, Int)] = None
//  var mouseUp: Option[(Int, Int)] = None
//    object PointingDevice {
//      //events
//      def click: Option[(Int, Int)] = inputs.mouseClick.orElse(inputs.touchPoint)
//      def down: Option[(Int, Int)] = inputs.mouseDown.orElse(inputs.touchDown)
//      def up: Option[(Int, Int)] = inputs.mouseUp.orElse(inputs.touchUp)
//      def drag: Option[(Float, Float)] = inputs.touchScrollVector
//
//}
