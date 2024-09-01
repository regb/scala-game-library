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

  case object KeyPressed extends EventType
  case object KeyReleased extends EventType
}

trait AWTInputProvider {
  this: AWTWindowProvider =>

  
  private val pool = new Pool(() => new InputEvent, 15)

  val inputEventsQueue = new Queue[InputEvent]


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
      }

      pool.release(event)
    }
  }

  private def mouseEventButton(e: MouseEvent): Input.MouseButtons.MouseButton = {
    if(javax.swing.SwingUtilities.isLeftMouseButton(e))
      Input.MouseButtons.Left
    else if(javax.swing.SwingUtilities.isRightMouseButton(e))
      Input.MouseButtons.Right
    else if(javax.swing.SwingUtilities.isRightMouseButton(e))
      Input.MouseButtons.Middle
    else
      Input.MouseButtons.Left
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

    gameCanvas.addKeyListener(new KeyListener() {
      private var currentlyPressed = new HashSet[Int]

      override def keyPressed(e: KeyEvent): Unit = {
        // keyPressed event will actually automatically repeat the key event
        // if it's remaining pressed for some time. This seems to be an OS-behavior
        // and might even be JVM-platform-dependent. For us, all we want is to
        // prevent such a repeat from being exposed to the game code, so we detect
        // these and filter out.
        // Also, as far as my tests go, even if the KeyPressed is repeated, the
        // KeyReleased event will only occur once, at the very end.
        if(currentlyPressed.contains(e.getKeyCode))
          return
        currentlyPressed.add(e.getKeyCode)

        val event = pool.acquire()
        event.eventType = InputEvent.KeyPressed
        event.keyEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def keyReleased(e: KeyEvent): Unit = {
        currentlyPressed.remove(e.getKeyCode)
        val event = pool.acquire()
        event.eventType = InputEvent.KeyReleased
        event.keyEvent = e
        inputEventsQueue.enqueue(event)
      }
      override def keyTyped(e: KeyEvent): Unit = {}
    })

  }

}
