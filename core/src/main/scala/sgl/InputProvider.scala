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
 * should be provided directly in the InputProvider (but actually implemented
 * in the InputHelper) component, and could be queried at any time during update,
 * since there should not be any notion of who consumed the state first (unlike events).
 * State are typically whether a key is currently pressed or not, so it can remain
 * active for several frames. Events are one-off occurence, when the key is pressed
 * down and then nothing for several frames, until it is released, where there
 * would be another single event.
 */

/** Provider for input handling 
  *
  * The core responsability of an InputProvider implementation is to collect
  * system events {InputEvent}, and expose them in the eventQueue. The InputProvider
  * only collects lowest level events, such as mouse down and up, and not derived
  * event such as mouse pressed (down + up in a given interval). It also distinguishes
  * each kind of device (mouse vs touch), and does not abstract one as the other. That
  * job can be performed by a layer on top. The design principles here is to expose the
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
  *
  * The implementation of the input state is provided in another trait, the
  * InputHelpers, and is thus optional. It is a natural consequence of the above design,
  * as we do not want to necessarily force the handling of all events all the time, nor
  * the update of the global state. That layer is also a natural place for providing
  * higher-level events that require some buffering/history (another word for state),
  * such as tap (touch down and up, with a reasonable time in between) and scroll events.
  */
trait InputProvider {
  self: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl.input")

  /** Control if the events should be processed synchronously or asynchronously.
    *
    * This settings work in conjunction with the Input.setEventProcessor, if
    * there's no event processor then the settings is ignored. If true, the events 
    * will be processed just before the next update loop, in the same thread as
    * the main loop thread. If false, the events are handled immediately when they
    * happen, potentially on a different thread than the main execution thread.
    */
  val ProcessInputsDuringUpdate: Boolean = true

  object Input {
    import scala.collection.mutable.Queue

    // Provides a hook which is called whenever an event fires.
    private var eventProcessor: Option[(InputEvent) => Unit] = None

    /** Set an event processor.
      *
      * By default, there's no event processor set, and events will simply
      * accumulate in the eventQueue until explicitly processed by
      * pollEvent() or processEvents(). The eventProcessor can be set
      * so that the Input module automatically calls it on every event.
      *
      * Depending on the value of ProcessInputsDuringUpdate, the processor code
      * will be invoked synchronously on the event (if false), or
      * asynchronously on the event during the next loop iteration (if true).
      * If the processing is delayed to the next loop update, all the events
      * will automatically be processed, in the order of arrival, just before
      * the framework invokes the update() call.
      *
      * The difference when executed synchronously is that this is called
      * in the same callback/thread as the event happening. Processing
      * done here will be immediately effective in response to the
      * event, but that also means it will happen outside an update-render
      * loop cycle. This can be particularly useful if you target javascript,
      * because browsers tend to require that some actions (opening links,
      * playing music) happen in the same callback as the event, and
      * using the eventQueue model to process events in batch does not actually
      * work well with that requirements (the action happens in a different
      * callstack than the user event).
      *
      * This has the drawback of course that these callbacks might end up racing
      * with a loop update call. So either you need to disable the
      * ProcessInputsDuringUpdate (most platforms set it to false by default), or
      * you need to be aware of the potential races. It is not an issue on the web,
      * because there's no multithreading, and thus this is a platform where this
      * would be enabled by default.
      */
    def setEventProcessor(processor: (InputEvent) => Unit): Unit = synchronized {
      eventProcessor = Some(processor)
    }
    def clearEventProcessor(): Unit = synchronized {
      eventProcessor = None
    }
    // TODO: Maybe it would be nice to support a list of event processors, so that
    // several processors can be added by different modules, and we would try to
    // apply each of them to the events until we find one that processes the event.
    // If none of the processors are able to handle the event, then the event stays
    // in the queue, just like when no processor is set.

    private val eventQueue: Queue[InputEvent] = new Queue[InputEvent]()

    private[sgl] def newEvent(event: InputEvent): Unit = synchronized {
      if(ProcessInputsDuringUpdate || eventProcessor.isEmpty) {
        logger.info("Adding new event: " + event + " to the queue.")
        eventQueue.enqueue(event)
      } else {
        logger.info("executing event right away")
        eventProcessor.get(event)
      }
    }

    // Called by the loop on each update, if ProcessInputsDuringUpdate is set
    // and we have a processor, we will process all the events through the processor.
    // If no eventProcessor is set, we just leave the events in the queue and they
    // need to be handled by the game explicitly.
    private[sgl] def onLoopUpdate(): Unit = synchronized {
      if(ProcessInputsDuringUpdate) eventProcessor.foreach(p => {
        while(Input.eventQueue.nonEmpty)
          p(Input.eventQueue.dequeue)
      })
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

    case class SystemActionEvent(action: Actions.Action) extends InputEvent

    /*
     * Touch events: Moved means that the cursor is currently touching (different interpretation
     * from the mouse, where it just means moving and not necessarly pressed). The pointer is
     * the id of the pointer that is touching, to support multi-touch.
     *
     * These seem to be the three fundamental sorts of events coming from a touch device.
     * Events such as dragged/scrolled can be built on top of them by detecting a
     * combination of Down and Moved. The argument for not exposing these higher-level
     * events is that their interpretation is subjective, in particular the duration
     * of the down-move-up sequence might indicate either a click or a dragged event,
     * depending on sensitivity/settings required by the gameplay or the user. On the
     * other hand, the 3 basic type of events are fundamentally non controversial and
     * can be expose safely in the core API.
     */

    sealed trait TouchInputEvent extends InputEvent
    case class TouchMovedEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchDownEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent
    case class TouchUpEvent(x: Int, y: Int, pointer: Int) extends TouchInputEvent

    /*
     * Then, on top of these primitive events, we should provide some higher-level
     * abstractions, mostly in the form of Extractors, which can interepret
     * these events as a more convenient concept from the gameplay point of view.
     * These here will be limited to abstractions that only depend on the current
     * event and not a sequence of events to be interpreted.
     *
     * One difficult abstraction is sequence of events, such as scrolling with
     * the finger or dragging with the mouse. Besides needing some history state,
     * there's some challenge in processing the inputs. In a traditional game loop
     * architecture, the inputs will be processed once per frame, but the issue is
     * they will all appear as if they arrived just on the frame boundary, although they
     * were happening all throughout the frame. This is common for a sequence of
     * moved mouse or pointer, because without the exact timestamp of each of them
     * it becomes difficult to tell the velocity of the move.
     */

    /** Abstraction on top of mouse and touch.
      *
      * Provide an extractor that can transparently handle mouse and touch
      * events.
      *
      * Note that this does not expose left/right clicks of the mouse, they
      * both look as if they came from the same pointer. There are good reasons
      * for that, in particular it would not be clear how to handle the
      * PointerMovedEvent when we do not know which pointer we should use
      * (the left one or the right one?). This does lead to some unexpected
      * sequences such as down-moved-moved-down-up-moved-up coming from the
      * same mouse pointer, due to the possibility of clicking both left and
      * right buttons.
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
    }

    object Actions {
      /** A general action input depending on the system.
        *
        * Typical examples would be clicking the back or home
        * button on Android. These are inputs that are better
        * described as triggered or not (by opposition to keys
        * which are Down/Up events) and they have a some sort of
        * semantic meaning (like back on android meaning going back
        * to the up activity/screen).
        */
      sealed trait Action

      case object Back extends Action
      case object Menu extends Action
    }
  }


}
