package com.regblanc.sgl.test
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameStateComponent
  with WindowProvider with InputHelpersComponent with GameLoopStatisticsComponent
  with LoggingProvider with SystemProvider =>

  private implicit val LogTag = Logger.Tag("main-screen")

  object LoadingScreen extends GameScreen {
    override def name: String = "Loading Screen"

    var characterBitmap: Option[Bitmap] = None

    private var characterBitmapLoader: Loader[Bitmap] = null

    override def update(dt: Long): Unit = {
      println("loading")
      if(characterBitmapLoader == null) {
        characterBitmapLoader = Graphics.loadImage(System.ResourcesPrefix / "drawable" / "character.png")
      }
      if(characterBitmapLoader.isLoaded) {
        characterBitmap = Some(characterBitmapLoader.value.get.get)
        gameState.newScreen(new MainScreen)
      }
    }
    override def render(canvas: Canvas): Unit = {}
  }

  class MainScreen extends GameScreen {

    override def name: String = "TestScreen"

    private val characterBitmap = LoadingScreen.characterBitmap.get
    private val characterFrames = Array(
      BitmapRegion(characterBitmap, 0, 0, dp2px(48), dp2px(68)),
      BitmapRegion(characterBitmap, dp2px(48), 0, dp2px(48), dp2px(68)),
      BitmapRegion(characterBitmap, dp2px(96), 0, dp2px(48), dp2px(68)),
      BitmapRegion(characterBitmap, dp2px(144), 0, dp2px(48), dp2px(68))
    )
    val characterAnimation = new Animation(200, characterFrames, Animation.Loop)

    var x: Double = 0
    var y: Double = 0

    var autoX = 0d
    var autoY = 0d

    var totalTime: Long = 0
    override def update(dt: Long): Unit = {
      InputHelpers.processEvents(e => e match {
        case Input.PointerDownEvent(x, y, _) =>
          this.x = x
          this.y = y
        case _ => ()
      })
      totalTime += dt

      if(Inputs.Keyboard.left) {
        x -= dp2px(50)*(dt/1000d)
      }
      if(Inputs.Keyboard.right) {
        x += dp2px(50)*(dt/1000d)
      }

      if(Inputs.Keyboard.up) {
        y -= dp2px(50)*(dt/1000d)
      }
      if(Inputs.Keyboard.down) {
        y += dp2px(50)*(dt/1000d)
      }

      autoX += dp2px(50)*(dt/1000d)
      autoY += dp2px(50)*(dt/1000d)

      //logger.debug("Fps: " + statistics.measuredFps)
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, defaultPaint.withColor(Color.rgb(204, 242, 204)))
      canvas.drawCircle(autoX.toInt, autoY.toInt, dp2px(50), defaultPaint.withColor(Color.Black))

      canvas.drawBitmap(characterAnimation.currentFrame(totalTime), x.toInt, y.toInt)
    }

  }

}
