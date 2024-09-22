package com.regblanc.sgl.board
package core

import sgl._
import geometry._
import scene._
import scene.ui._
import util._

trait ScreensComponent {
  this: GraphicsProvider with SystemProvider with WindowProvider 
  with GameStateComponent with LoggingProvider with ViewportComponent =>

  private implicit val LogTag = Logger.Tag("main-screen")

  class BoardScreen extends GameScreen with InputProcessor {

    override def name: String = "board-screen"

    val BoardSize: Int = 1
    val WorldHeight: Int = 10
    val WorldWidth: Float = Window.width*(WorldHeight/Window.height.toFloat)

    val viewport = new Viewport(Window.width, Window.height)
    viewport.setCamera(0, 0, WorldWidth.toFloat, WorldHeight.toFloat)
    viewport.scalingStrategy = Viewport.Fit

    val p = (0f, 0f)

    override def keyDown(e: Input.Keys.Key): Boolean = {
      e match {
        case Input.Keys.Down =>
          viewport.translateCamera(0, 0.5f)
        case Input.Keys.Up =>
          viewport.translateCamera(0, -0.5f)
        case Input.Keys.Left =>
          viewport.translateCamera(-0.5f, 0)
        case Input.Keys.Right =>
          viewport.translateCamera(0.5f, 0)
        case _ =>
      }
      true
    }

    Input.setInputProcessor(this)

    override def update(dt: Long): Unit = { }

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawRect(0, 0, Window.width, Window.height, Graphics.defaultPaint.withColor(Graphics.Color.Blue))
      viewport.withViewport(canvas) {
        for(i <- 0 until 100) {
          for(j <- 0 until 100) {
	        val color = if((i+j) % 2 == 0) Graphics.Color.White else Graphics.Color.Black
	        canvas.drawRect(j.toFloat, i.toFloat, 1f, 1f, Graphics.defaultPaint.withColor(color))
	      }
	    }
	    canvas.drawCircle(p._1 + 0.5f, p._2 + 0.5f, 0.5f, Graphics.defaultPaint.withColor(Graphics.Color.Green))
      }
    }

  }

}
