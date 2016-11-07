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

    private val characterBitmap = loadImageFromResource("character.png")
    private val characterFrames = Array(
      BitmapRegion(characterBitmap, 0, 0, 48, 68),
      BitmapRegion(characterBitmap, 48, 0, 48, 68),
      BitmapRegion(characterBitmap, 96, 0, 48, 68),
      BitmapRegion(characterBitmap, 144, 0, 48, 68)
    )
    val characterAnimation = new Animation(200, characterFrames, Animation.Loop)

    var x: Double = 0
    var y: Double = 0

    var autoX = 0d
    var autoY = 0d

    var totalTime: Long = 0
    override def update(dt: Long): Unit = {
      InputHelpers.processEvents(e => println("got event: " + e))

      totalTime += dt

      if(Inputs.Keyboard.left) {
        x -= 50*(dt/1000d)
      }
      if(Inputs.Keyboard.right) {
        x += 50*(dt/1000d)
      }

      if(Inputs.Keyboard.up) {
        y -= 50*(dt/1000d)
      }
      if(Inputs.Keyboard.down) {
        y += 50*(dt/1000d)
      }

      autoX += 50*(dt/1000d)
      autoY += 50*(dt/1000d)
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, defaultPaint.withColor(Color.rgb(204, 242, 204)))
      canvas.drawCircle(autoX.toInt, autoY.toInt, 50, defaultPaint.withColor(Color.Black))

      canvas.drawBitmap(characterAnimation.currentFrame(totalTime), x.toInt, y.toInt)
    }

  }

}
