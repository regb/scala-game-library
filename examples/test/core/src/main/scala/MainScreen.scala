package com.regblanc.sgl.test
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameStateComponent
  with WindowProvider with SystemProvider with AudioProvider =>

  class MainScreen extends GameScreen {

    var x: Double = 0
    var y: Double = 0

    override def update(dt: Long): Unit = {
      println("updating")
      x += 10*(dt/1000d)
      y += 10*(dt/1000d)
    }

    override def render(canvas: Canvas): Unit = {
      println("drawing rect")

      canvas.drawRect(0, 0, WindowWidth, WindowHeight, defaultPaint.withColor(Color.rgb(204, 242, 204)))

      println("drawing circle")
      canvas.drawCircle(x.toInt, y.toInt, 50, defaultPaint.withColor(Color.Black))
    }

  }

}
