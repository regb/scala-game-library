package sgl
package html5


trait Html5InputProvider extends InputProvider with Lifecycle {
  this: Html5WindowProvider =>

  abstract override def startup(): Unit = {
    //gamePanel.addMouseListener(new MouseAdapter() {
    //  override def mouseClicked(e: MouseEvent): Unit = { }
    //  override def mousePressed(e: MouseEvent): Unit = {
    //    Input.newEvent(Input.MouseDownEvent(e.getX, e.getY, mouseEventButton(e)))
    //  }
    //  override def mouseReleased(e: MouseEvent): Unit = {
    //    Input.newEvent(Input.MouseUpEvent(e.getX, e.getY, mouseEventButton(e)))
    //  }
    //})
    //gamePanel.addMouseMotionListener(new MouseAdapter() {
    //  //mouseMoved is only when not pressed, while dragged is only
    //  //when pressed. We abstract both into a MouseMovedEvent, and
    //  //the dragged can be detected with the MouseDownEvent happening
    //  //before
    //  override def mouseDragged(e: MouseEvent): Unit = {
    //    Input.newEvent(Input.MouseMovedEvent(e.getX, e.getY))
    //  }
    //  override def mouseMoved(e: MouseEvent): Unit = {
    //    Input.newEvent(Input.MouseMovedEvent(e.getX, e.getY))
    //  }
    //})

    //gamePanel.addKeyListener(new KeyListener() {
    //  override def keyPressed(e: KeyEvent): Unit = {
    //    keyEventKey(e).foreach(key => {
    //      Input.newEvent(Input.KeyDownEvent(key))
    //    })
    //  }
    //  override def keyReleased(e: KeyEvent): Unit = {
    //    keyEventKey(e).foreach(key => {
    //      Input.newEvent(Input.KeyUpEvent(key))
    //    })
    //  }
    //  override def keyTyped(e: KeyEvent): Unit = {}
    //})

    super.startup()
  }
  abstract override def shutdown(): Unit = {
    super.shutdown()
  }

}
