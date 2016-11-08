package sgl
package html5

import org.scalajs.dom
import dom.html

trait Html5InputProvider extends InputProvider with Lifecycle {
  this: Html5WindowProvider with Html5GraphicsProvider =>

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

  abstract override def startup(): Unit = {
    this.canvas.onmousedown = (e: dom.MouseEvent) => {
      Input.newEvent(Input.MouseDownEvent(e.clientX.toInt, e.clientY.toInt, mouseEventButton(e)))
    }
    this.canvas.onmouseup = (e: dom.MouseEvent) => {
      Input.newEvent(Input.MouseUpEvent(e.clientX.toInt, e.clientY.toInt, mouseEventButton(e)))
    }
    this.canvas.onmousemove = (e: dom.MouseEvent) => {
      Input.newEvent(Input.MouseMovedEvent(e.clientX.toInt, e.clientY.toInt))
    }

    dom.document.onkeydown = (e: dom.KeyboardEvent) => {
      domEventToKey(e).foreach(key =>
        Input.newEvent(Input.KeyDownEvent(key))
      )
    }
    dom.document.onkeyup = (e: dom.KeyboardEvent) => {
      domEventToKey(e).foreach(key =>
        Input.newEvent(Input.KeyUpEvent(key))
      )
    }

    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
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
