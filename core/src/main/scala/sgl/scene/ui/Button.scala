package sgl
package scene
package ui

trait ButtonsComponent {
  this: GraphicsProvider with SceneComponent =>

  import Graphics._

  class Button(_x: Int, _y: Int, regularBitmap: BitmapRegion, pressedBitmap: BitmapRegion)
    extends SceneNode(_x, _y, regularBitmap.width, regularBitmap.height) {

    private var pressed: Boolean = false

    override def update(dt: Long): Unit = {}

    override def render(canvas: Canvas): Unit = {
      if(pressed)
        canvas.drawBitmap(pressedBitmap, x.toInt, y.toInt)
      else
        canvas.drawBitmap(regularBitmap, x.toInt, y.toInt)
    }

    override def notifyDown(x: Int, y: Int): Boolean = {
      pressed = true
      true
    }
    override def notifyPointerLeave(): Unit = {
      pressed = false
    }
    override def notifyUp(x: Int, y: Int): Boolean = {
      pressed = false
      true
    }

  }

}


