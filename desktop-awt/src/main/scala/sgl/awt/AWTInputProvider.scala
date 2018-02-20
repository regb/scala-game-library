package sgl
package awt

import java.awt.event._
import sgl.util._

trait AWTInputProvider extends InputProvider {
  this: AWTWindowProvider with LoggingProvider =>

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
    gamePanel.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent): Unit = { }
      override def mousePressed(e: MouseEvent): Unit = {
        Input.newEvent(Input.MouseDownEvent(e.getX, e.getY, mouseEventButton(e)))
      }
      override def mouseReleased(e: MouseEvent): Unit = {
        Input.newEvent(Input.MouseUpEvent(e.getX, e.getY, mouseEventButton(e)))
      }
    })
    gamePanel.addMouseMotionListener(new MouseAdapter() {
      //mouseMoved is only when not pressed, while dragged is only
      //when pressed. We abstract both into a MouseMovedEvent, and
      //the dragged can be detected with the MouseDownEvent happening
      //before
      override def mouseDragged(e: MouseEvent): Unit = {
        Input.newEvent(Input.MouseMovedEvent(e.getX, e.getY))
      }
      override def mouseMoved(e: MouseEvent): Unit = {
        Input.newEvent(Input.MouseMovedEvent(e.getX, e.getY))
      }
    })

    gamePanel.addKeyListener(new KeyListener() {
      override def keyPressed(e: KeyEvent): Unit = {
        keyEventKey(e).foreach(key => {
          Input.newEvent(Input.KeyDownEvent(key))
        })
      }
      override def keyReleased(e: KeyEvent): Unit = {
        keyEventKey(e).foreach(key => {
          Input.newEvent(Input.KeyUpEvent(key))
        })
      }
      override def keyTyped(e: KeyEvent): Unit = {}
    })

  }

}
