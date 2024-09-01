package com.regblanc.sgl.menu
package core

import sgl._
import geometry._
import scene._
import scene.ui._
import util._

trait ScreensComponent {
  this: GraphicsProvider with SystemProvider with WindowProvider 
  with GameStateComponent with LoggingProvider
  with ViewportComponent with SceneComponent with PopupsComponent =>

  private implicit val LogTag = Logger.Tag("main-screen")

  class LevelsScreen extends GameScreen {

    override def name: String = "LevelsScreen"

    val viewport = new Viewport(Window.width, Window.height)

    val scene = new SceneGraph(Window.width, Window.height, viewport)

    val levelsPane = new ScrollPane(0, 0, Window.width, Window.height, Window.width, 3*Window.height)
    scene.addNode(levelsPane)

    class LevelButton(i: Int, _x: Float, _y: Float) extends Button(_x, _y, 100, 30) {
      override def notifyClick(x: Float, y: Float): Unit = {
        println(s"button $i clicked at ($x, $y)")
      }
      override def renderPressed(canvas: Graphics.Canvas): Unit = {
        val color = Graphics.defaultPaint.withColor(Graphics.Color.Red)
        canvas.drawRect(x, y, width, height, color)
      }
      override def renderRegular(canvas: Graphics.Canvas): Unit = {
        val color = Graphics.defaultPaint.withColor(Graphics.Color.Green)
        canvas.drawRect(x, y, width, height, color)
      }

      override def notifyMoved(x: Float, y: Float): Unit = {
        println("moved")
      }
    }
    for(i <- 1 to 100) {
      val button = new LevelButton(i, 20, (i*50).toFloat)
      levelsPane.addNode(button)
    }
    val dialog =
      new DialogPopup(
        Window.width, Window.height,
        new Dialog(Window.dp2px(400),
                   "Hey there, do you like the weather ok?",
                   List(("Yes", () => { println("yes") }),
                        ("Nope", () => { println("nope") }),
                        ("Meh", () => { println("meh") })),
                   Window.dp2px(36), Graphics.Color.White)
      ) {
        override val backgroundColor = Graphics.Color.rgba(0,0,0,150)
      }
    scene.addNode(dialog)

    Input.setInputProcessor(scene)
            //case Input.KeyDownEvent(Input.Keys.P) => dialog.show()

    override def update(dt: Long): Unit = {
      scene.update(dt)
    }

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawColor(Graphics.Color.Black)
      scene.render(canvas)
    }

  }

}
