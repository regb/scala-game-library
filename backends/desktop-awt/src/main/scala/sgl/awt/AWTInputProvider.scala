package sgl
package awt

import java.awt.event._
import sgl.util._

import scala.collection.mutable.{Queue, HashSet}

// This is an internal object that we re-use as part of an object pool. Its
// fields will always be overriden when a new instance is created.
class InputEvent {
  var eventType: InputEvent.EventType = InputEvent.MousePressed
  var mouseEvent: MouseEvent = null
  var keyEvent: KeyEvent = null
}

object InputEvent {
  sealed trait EventType
  case object MousePressed extends EventType
  case object MouseReleased extends EventType
  case object MouseMoved extends EventType
  case object MouseScrolled extends EventType

  case object KeyPressed extends EventType
  case object KeyReleased extends EventType
  case object KeyRepeated extends EventType
  case object KeyTyped extends EventType
}

trait AWTInputProvider {
  this: AWTWindowProvider with CanvasProvider with SystemProvider with LoggingProvider =>

  private val pool = new Pool(() => new InputEvent, 15)

  val inputEventsQueue = new Queue[InputEvent]

  private val typedText = new AWTTypedText

  def processInputEvents(): Unit = {
    while(inputEventsQueue.nonEmpty) {
      val event = inputEventsQueue.dequeue()

      event.eventType match {
        case InputEvent.MousePressed => {
          val e = event.mouseEvent
          Input.inputProcessor.mouseDown(e.getX, e.getY, mouseEventButton(e))
        }
        case InputEvent.MouseReleased => {
          val e = event.mouseEvent
          Input.inputProcessor.mouseUp(e.getX, e.getY, mouseEventButton(e))
        }
        case InputEvent.MouseMoved => {
          val e = event.mouseEvent
          Input.inputProcessor.mouseMoved(e.getX, e.getY)
        }
        case InputEvent.MouseScrolled => {
          val e = event.mouseEvent.asInstanceOf[MouseWheelEvent]
          Input.inputProcessor.mouseScrolled(0f, e.getPreciseWheelRotation.toFloat, Input.ScrollUnits.Notches)
        }
        case InputEvent.KeyPressed => {
          val e = event.keyEvent
          keyEventKey(e).foreach(key => {
            Input.inputProcessor.keyDown(key)
          })
        }
        case InputEvent.KeyReleased => {
          val e = event.keyEvent
          keyEventKey(e).foreach(key => {
            Input.inputProcessor.keyUp(key)
          })
        }
        case InputEvent.KeyRepeated => {
          keyEventKey(event.keyEvent).foreach(key => {
            Input.inputProcessor.keyRepeated(key)
          })
        }
        case InputEvent.KeyTyped => {
          typedText.add(event.keyEvent).foreach(text => {
            Input.inputProcessor.textInput(text)
          })
        }
      }

      pool.release(event)
    }
  }

  private def mouseEventButton(e: MouseEvent): Input.MouseButtons.MouseButton = e.getButton match {
    case MouseEvent.BUTTON1 => Input.MouseButtons.Left
    case MouseEvent.BUTTON2 => Input.MouseButtons.Middle
    case MouseEvent.BUTTON3 => Input.MouseButtons.Right
    case other => Input.MouseButtons.Other(other)
  }

  // TODO: probably leads to lots of garbage collection, we should not create all
  // these Some objects.
  // TODO: there must be a way to turn a partial match into a map structure?
  private def keyEventKey(e: KeyEvent): Option[Input.Keys.Key] = e.getKeyCode match {
    case KeyEvent.VK_SPACE => Some(Input.Keys.Space)

    case KeyEvent.VK_LEFT => Some(Input.Keys.Left)
    case KeyEvent.VK_UP => Some(Input.Keys.Up)
    case KeyEvent.VK_RIGHT => Some(Input.Keys.Right)
    case KeyEvent.VK_DOWN => Some(Input.Keys.Down)

    case KeyEvent.VK_A => Some(Input.Keys.A)
    case KeyEvent.VK_B => Some(Input.Keys.B)
    case KeyEvent.VK_C => Some(Input.Keys.C)
    case KeyEvent.VK_D => Some(Input.Keys.D)
    case KeyEvent.VK_E => Some(Input.Keys.E)
    case KeyEvent.VK_F => Some(Input.Keys.F)
    case KeyEvent.VK_G => Some(Input.Keys.G)
    case KeyEvent.VK_H => Some(Input.Keys.H)
    case KeyEvent.VK_I => Some(Input.Keys.I)
    case KeyEvent.VK_J => Some(Input.Keys.J)
    case KeyEvent.VK_K => Some(Input.Keys.K)
    case KeyEvent.VK_L => Some(Input.Keys.L)
    case KeyEvent.VK_M => Some(Input.Keys.M)
    case KeyEvent.VK_N => Some(Input.Keys.N)
    case KeyEvent.VK_O => Some(Input.Keys.O)
    case KeyEvent.VK_P => Some(Input.Keys.P)
    case KeyEvent.VK_Q => Some(Input.Keys.Q)
    case KeyEvent.VK_R => Some(Input.Keys.R)
    case KeyEvent.VK_S => Some(Input.Keys.S)
    case KeyEvent.VK_T => Some(Input.Keys.T)
    case KeyEvent.VK_U => Some(Input.Keys.U)
    case KeyEvent.VK_V => Some(Input.Keys.V)
    case KeyEvent.VK_W => Some(Input.Keys.W)
    case KeyEvent.VK_X => Some(Input.Keys.X)
    case KeyEvent.VK_Y => Some(Input.Keys.Y)
    case KeyEvent.VK_Z => Some(Input.Keys.Z)

    case KeyEvent.VK_ENTER =>
      Some(if(e.getKeyLocation == KeyEvent.KEY_LOCATION_NUMPAD) Input.Keys.NumpadEnter else Input.Keys.Enter)
    case KeyEvent.VK_ESCAPE => Some(Input.Keys.Escape)
    case KeyEvent.VK_BACK_SPACE => Some(Input.Keys.Backspace)
    case KeyEvent.VK_SHIFT =>
      Some(if(e.getKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) Input.Keys.RightShift else Input.Keys.LeftShift)
    case KeyEvent.VK_CONTROL =>
      Some(if(e.getKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) Input.Keys.RightControl else Input.Keys.LeftControl)
    case KeyEvent.VK_ALT =>
      Some(if(e.getKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) Input.Keys.RightAlt else Input.Keys.LeftAlt)
    case KeyEvent.VK_ALT_GRAPH => Some(Input.Keys.RightAlt)
    case KeyEvent.VK_TAB => Some(Input.Keys.Tab)
    case KeyEvent.VK_DELETE => Some(Input.Keys.Delete)
    case KeyEvent.VK_HOME => Some(Input.Keys.Home)
    case KeyEvent.VK_END => Some(Input.Keys.End)
    case KeyEvent.VK_PAGE_UP => Some(Input.Keys.PageUp)
    case KeyEvent.VK_PAGE_DOWN => Some(Input.Keys.PageDown)
    case KeyEvent.VK_F1 => Some(Input.Keys.F1)
    case KeyEvent.VK_F2 => Some(Input.Keys.F2)
    case KeyEvent.VK_F3 => Some(Input.Keys.F3)
    case KeyEvent.VK_F4 => Some(Input.Keys.F4)
    case KeyEvent.VK_F5 => Some(Input.Keys.F5)
    case KeyEvent.VK_F6 => Some(Input.Keys.F6)
    case KeyEvent.VK_F7 => Some(Input.Keys.F7)
    case KeyEvent.VK_F8 => Some(Input.Keys.F8)
    case KeyEvent.VK_F9 => Some(Input.Keys.F9)
    case KeyEvent.VK_F10 => Some(Input.Keys.F10)
    case KeyEvent.VK_F11 => Some(Input.Keys.F11)
    case KeyEvent.VK_F12 => Some(Input.Keys.F12)

    case KeyEvent.VK_0 => Some(Input.Keys.Num0)
    case KeyEvent.VK_1 => Some(Input.Keys.Num1)
    case KeyEvent.VK_2 => Some(Input.Keys.Num2)
    case KeyEvent.VK_3 => Some(Input.Keys.Num3)
    case KeyEvent.VK_4 => Some(Input.Keys.Num4)
    case KeyEvent.VK_5 => Some(Input.Keys.Num5)
    case KeyEvent.VK_6 => Some(Input.Keys.Num6)
    case KeyEvent.VK_7 => Some(Input.Keys.Num7)
    case KeyEvent.VK_8 => Some(Input.Keys.Num8)
    case KeyEvent.VK_9 => Some(Input.Keys.Num9)

    case _ => None
  }

  def registerInputListeners(): Unit = {
    gameCanvas.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent): Unit = { }
      override def mousePressed(e: MouseEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.MousePressed
        event.mouseEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def mouseReleased(e: MouseEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.MouseReleased
        event.mouseEvent = e
        inputEventsQueue.enqueue(event)
      }
    })
    gameCanvas.addMouseMotionListener(new MouseAdapter() {
      //mouseMoved is only when not pressed, while dragged is only
      //when pressed. We abstract both into a MouseMovedEvent, and
      //the dragged can be detected with the MouseDownEvent happening
      //before
      override def mouseDragged(e: MouseEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.MouseMoved
        event.mouseEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def mouseMoved(e: MouseEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.MouseMoved
        event.mouseEvent = e
        inputEventsQueue.enqueue(event)
      }
    })

    gameCanvas.addMouseWheelListener(new MouseAdapter() {
      override def mouseWheelMoved(e: MouseWheelEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.MouseScrolled
        event.mouseEvent = e
        inputEventsQueue.enqueue(event)
      }
    })

    // Tab is a key like others, instead of moving the focus.
    gameCanvas.setFocusTraversalKeysEnabled(false)
    gameCanvas.addKeyListener(new KeyListener() {
      // By location too, as both shift keys have the same code.
      private val currentlyPressed = new HashSet[(Int, Int)]
      private def pressed(e: KeyEvent): (Int, Int) = (e.getKeyCode, e.getKeyLocation)

      override def keyPressed(e: KeyEvent): Unit = {
        // keyPressed event will actually automatically repeat the key event
        // if it's remaining pressed for some time. This seems to be an OS-behavior
        // and might even be JVM-platform-dependent. The game is told of a repeat
        // as such, so we detect these.
        // Also, as far as my tests go, even if the KeyPressed is repeated, the
        // KeyReleased event will only occur once, at the very end.
        val event = pool.acquire()
        event.eventType = if(currentlyPressed.add(pressed(e))) InputEvent.KeyPressed else InputEvent.KeyRepeated
        event.keyEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def keyReleased(e: KeyEvent): Unit = {
        currentlyPressed.remove(pressed(e))
        val event = pool.acquire()
        event.eventType = InputEvent.KeyReleased
        event.keyEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def keyTyped(e: KeyEvent): Unit = {
        val event = pool.acquire()
        event.eventType = InputEvent.KeyTyped
        event.keyEvent = e
        inputEventsQueue.enqueue(event)
      }
    })

  }

}

/** Makes text of what AWT says is typed. It comes by the char, so that the
  * characters that are made of two come in halves.
  */
private[awt] final class AWTTypedText(onMac: Boolean = java.lang.System.getProperty("os.name").startsWith("Mac")) {
  private var firstHalf: Option[Char] = None

  /** The text that is complete with this event, if any. */
  def add(e: KeyEvent): Option[String] = {
    val c = e.getKeyChar
    // AltGr is control and alt together on Windows, and types characters as the
    // alt of a Mac does.
    val shortcut = e.isMetaDown || (!e.isAltGraphDown && (e.isControlDown || (e.isAltDown && !onMac)))
    val before = firstHalf
    firstHalf = None
    if(c == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(c) || shortcut) None
    else if(Character.isHighSurrogate(c)) {
      firstHalf = Some(c)
      None
    } else if(Character.isLowSurrogate(c)) before.map(first => s"$first$c")
    else Some(c.toString)
  }
}
