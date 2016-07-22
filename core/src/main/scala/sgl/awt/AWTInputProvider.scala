package sgl
package awt

import java.awt.event._

trait AWTInputProvider extends InputProvider with Lifecycle {
  this: AWTWindowProvider =>

  abstract override def startup(): Unit = {
    gamePanel.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent): Unit = {
        inputBuffer.mouseClick = Some((e.getX, e.getY))
      }
      override def mousePressed(e: MouseEvent): Unit = {
        inputBuffer.mouseDown = Some((e.getX, e.getY))
        inputBuffer.mousePressed = Some((e.getX, e.getY))
      }
      override def mouseReleased(e: MouseEvent): Unit = {
        inputBuffer.mouseUp = Some((e.getX, e.getY))
        inputBuffer.mousePressed = None
      }
    })
    gamePanel.addMouseMotionListener(new MouseAdapter() {
      override def mouseDragged(e: MouseEvent): Unit = {
        inputBuffer.mousePressed = Some((e.getX, e.getY))
        //TODO: mouse scroll vector
      }
      override def mouseMoved(e: MouseEvent): Unit = {
        inputBuffer.mousePosition = Some((e.getX, e.getY))
      }
    })

    gamePanel.addKeyListener(new KeyListener() {
      override def keyPressed(e: KeyEvent): Unit = {
        if(e.getKeyCode == KeyEvent.VK_LEFT)
          inputBuffer.Keyboard.left = true
        else if(e.getKeyCode == KeyEvent.VK_RIGHT)
          inputBuffer.Keyboard.right = true
        else if(e.getKeyCode == KeyEvent.VK_W)
          inputBuffer.Keyboard.w = true
      }
      override def keyReleased(e: KeyEvent): Unit = {
        if(e.getKeyCode == KeyEvent.VK_LEFT)
          inputBuffer.Keyboard.left = false
        else if(e.getKeyCode == KeyEvent.VK_RIGHT)
          inputBuffer.Keyboard.right = false
        else if(e.getKeyCode == KeyEvent.VK_W)
          inputBuffer.Keyboard.w = false
      }
      override def keyTyped(e: KeyEvent): Unit = {}
    })

    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
  }

}
