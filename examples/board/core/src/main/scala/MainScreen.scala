package com.regblanc.sgl.board
package core

import sgl._
import geometry._
import scene._
import scene.ui._
import util._

trait ScreensComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider 
  with GameStateComponent with LoggingProvider with ViewportComponent =>

  private implicit val LogTag = Logger.Tag("main-screen")

  class BoardScreen extends GameScreen {

    override def name: String = "board-screen"

    val BoardSize: Int = 1
    val WorldHeight: Int = 10
    val WorldWidth: Float = Window.width*(WorldHeight/Window.height.toFloat)

    val viewport = new Viewport(Window.width, Window.height)
    viewport.setCamera(0, 0, WorldWidth, WorldHeight)
    viewport.scalingStrategy = Viewport.Fit

    val p = (0f, 0f)

    def handleEvent(e: Input.InputEvent): Unit = e match {
      case Input.KeyDownEvent(Input.Keys.Down) =>
        viewport.translateCamera(0, 0.5f)
      case Input.KeyDownEvent(Input.Keys.Up) =>
        viewport.translateCamera(0, -0.5f)
      case Input.KeyDownEvent(Input.Keys.Left) =>
        viewport.translateCamera(-0.5f, 0)
      case Input.KeyDownEvent(Input.Keys.Right) =>
        viewport.translateCamera(0.5f, 0)
      case _ =>
    }
    Input.setEventProcessor(handleEvent)

    override def update(dt: Long): Unit = { }

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawColor(Graphics.Color.Red)
      viewport.withViewport(canvas) {
        for(i <- 0 until 100) {
	  for(j <- 0 until 100) {
	    val color = if((i+j) % 2 == 0) Graphics.Color.White else Graphics.Color.Black
	    canvas.drawRect(j, i, 1f, 1f, Graphics.defaultPaint.withColor(color))
	  }
	}
	canvas.drawCircle(p._1 + 0.5f, p._2 + 0.5f, 0.5f, Graphics.defaultPaint.withColor(Graphics.Color.Green))
      }
    }

  }

}
