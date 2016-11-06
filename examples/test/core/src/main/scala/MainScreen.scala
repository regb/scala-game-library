package com.regblanc.sgl.test
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameStateComponent
  with WindowProvider with SystemProvider with AudioProvider with InputHelpersComponent =>

  class MainScreen extends GameScreen {

    val img: Bitmap = loadImageFromResource("/character.png")

    var x: Double = 0
    var y: Double = 0

    openWebpage(new java.net.URI("http://www.google.com"))

    override def update(dt: Long): Unit = {
      InputHelpers.processEvents(e => println("got event: " + e))

      if(Inputs.Keyboard.right) {
        x += 10*(dt/1000d)
      }
      if(Inputs.Keyboard.down) {
        y += 10*(dt/1000d)
      }
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, defaultPaint.withColor(Color.rgb(204, 242, 204)))
      canvas.drawCircle(x.toInt, y.toInt, 50, defaultPaint.withColor(Color.Black))
      canvas.drawBitmap(img, 20, 20)
    }

  }

}
