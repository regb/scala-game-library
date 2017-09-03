package sgl

import util._

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

/** Provider for input handling 
  *
  * The core responsability of an InputProvider implementation is to collect
  * system events {InputEvent}, and expose them in the eventQueue. The InputProvider
  * only collects lowest level events, such as mouse down and up, and not derived
  * event such as mouse pressed (down + up in a given interval). It also distinguishes
  * each kind of device (mouse vs touch), and does not abstract one as the other. That
  * job can be performed by a layer on top. The design principle here is to expose the
  * real core of the events, and then build any simplification/abstraction as separate
  * modules on top. That seeems like a cleaner design, and will simplify the implementation
  * of input providers backends.
  *
  * The queue is not automatically cleaned, and events must be processed by invoking
  * Input.pollEvent that returns the first event in the queue. This is the GameScreen
  * responsability to collect and handle events on each update/frame, and if it does
  * not call pollEvent, then events will keep accumulating in the queue, which could lead
  * to delayed response to user input. That design seems better as, in case the game falls
  * behind in term of updating/rendering, it's better to let it decide how to handle
  * inputs that should have been handled a few frames ago (rather than just drop them from the
  * queue).
  */
trait InputProvider {
  self: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl.input")

  object Input {
    import scala.collection.mutable.Queue

    private val eventQueue: Queue[InputEvent] = new Queue[InputEvent]()

    //TODO: this should be private, only accessible to the backends implementing the input
    //      provider
    def newEvent(event: InputEvent): Unit = synchronized {
      logger.trace("Adding new event: " + event + " to the queue.")
      eventQueue.enqueue(event)
    }

    /** Poll for the oldest non processed event.
      *
      * check if any events is present, and return an Option of an event.
      * The event is removed from the event queue.
      */
    def pollEvent(): Option[InputEvent] = synchronized {
      if(eventQueue.isEmpty) None else Some(eventQueue.dequeue())
    }

    /**
     * Functional approach to event handling. This method expects a partial function that handles
     * the incoming input.
     *
     * It is possible to provide a filter function through overloading that will dequeue only the Input
     * that satisfy the given condition, leaving the other into the queue.
     * @param function
     * @param filter
     */
    def processEvents(function:(InputEvent)=>Unit,filter:(InputEvent)=>Boolean): Unit= synchronized{
      eventQueue.dequeueAll(filter).foreach(function)
    }

    def processEvents(function:(InputEvent)=>Unit): Unit = processEvents(function,(x:InputEvent)=>true)

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

    //mouse events, no Dragged event as it can be derived as a combination of Moved and Down
    sealed trait MouseInputEvent extends InputEvent
    case class MouseMovedEvent(x: Int, y: Int) extends MouseInputEvent
    case class MouseDownEvent(x: Int, y: Int, mouseButton: MouseButtons.MouseButton) extends MouseInputEvent
    case class MouseUpEvent(x: Int, y: Int, mouseButton: MouseButtons.MouseButton) extends MouseInputEvent
    //middle button scrolled, amount +/- depending on direction
    case class MouseScrolledEvent(amount: Int) extends MouseInputEvent

    //touch events, Moved means that the cursor is currently touching (different interpretation
    //from the mouse, where it just means moving and not necessarly pressed). The pointer is
    //the id of the pointer that is touching, to support multi-touch
    sealed trait TouchInputEvent extends InputEvent
    case class TouchMovedEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchDownEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchUpEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent

    /** Abstraction on top of mouse and touch
      *
      * Provide an extractor that can transparently handle mouse and touch
      * events.
      */
    object PointerDownEvent {
      def unapply(event: InputEvent): Option[(Int, Int, Int)] = event match {
        case MouseDownEvent(x, y, _) => Some((x, y, 1))
        case TouchDownEvent(x, y, p) => Some((x, y, p))
        case _ => None
      }
    }
    object PointerUpEvent {
      def unapply(event: InputEvent): Option[(Int, Int, Int)] = event match {
        case MouseUpEvent(x, y, _) => Some((x, y, 1))
        case TouchUpEvent(x, y, p) => Some((x, y, p))
        case _ => None
      }
    }
    object PointerMovedEvent {
      def unapply(event: InputEvent): Option[(Int, Int, Int)] = event match {
        case MouseMovedEvent(x, y) => Some((x, y, 1))
        case TouchMovedEvent(x, y, p) => Some((x, y, p))
        case _ => None
      }
    }

    //TODO: seems like instead of using trait and case objects inheritence, we
    //      could use an abstract type MouseButton, and implement with proper
    //      implementation in each backend. For example, in the SDL-based implementation
    //      we would have type MouseButton = UByte, and return the UByte from SDL directly
    //      without the need for the conversion overhead
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

      /* @regb: NumKey can either be numpad numbers, or regular numbers
       *        I don't see the point of making the distinction
       */
      sealed trait NumKey extends Key {
        def toInt = this match {
          case Num0 => 0
          case Num1 => 1
          case Num2 => 2
          case Num3 => 3
          case Num4 => 4
          case Num5 => 5
          case Num6 => 6
          case Num7 => 7
          case Num8 => 8
          case Num9 => 9
        }
      }
      case object Num0 extends NumKey
      case object Num1 extends NumKey
      case object Num2 extends NumKey
      case object Num3 extends NumKey
      case object Num4 extends NumKey
      case object Num5 extends NumKey
      case object Num6 extends NumKey
      case object Num7 extends NumKey
      case object Num8 extends NumKey
      case object Num9 extends NumKey

      case object Space extends Key

      case object Left extends Key
      case object Up extends Key
      case object Right extends Key
      case object Down extends Key

      /*
       * Maybe we should have a Button object to contain these
       */
      case object ButtonStart extends Key
      case object ButtonSelect extends Key

      /*
       * We also have system buttons like back/menu in Android
       */
      case object ButtonBack extends Key
      case object ButtonMenu extends Key
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
