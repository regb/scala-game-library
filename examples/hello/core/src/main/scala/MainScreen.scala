package com.regblanc.sgl.test
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent extends ViewportComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider with AudioProvider
  with GameStateComponent with InputHelpersComponent with GameLoopStatisticsComponent
  with LoggingProvider with GraphicsHelpersComponent =>

  import Graphics.{Bitmap, Canvas, Color, BitmapRegion, Animation, RichCanvas}
  import Audio.Music
  import Window.dp2px

  private implicit val LogTag = Logger.Tag("main-screen")

  object LoadingScreen extends GameScreen {
    override def name: String = "Loading Screen"

    var characterBitmap: Option[Bitmap] = None
    var music: Option[Music] = None

    private var characterBitmapLoader: Loader[Bitmap] = null
    private var musicLoader: Loader[Music] = null

    override def update(dt: Long): Unit = {
      if(characterBitmapLoader == null) {
        characterBitmapLoader = Graphics.loadImage(ResourcesPrefix / "drawable" / "character.png")
      }
      if(musicLoader == null) {
        //musicLoader = Audio.loadMusic(ResourcesPrefix / "audio" / "music.wav")
        musicLoader = Audio.loadMusic(ResourcesPrefix / "audio" / "music.ogg")
      }
      if(characterBitmapLoader.isLoaded && musicLoader.isLoaded) {
        characterBitmap = Some(characterBitmapLoader.value.get.get)
        music = Some(musicLoader.value.get.get)
        music.foreach(_.play())
        gameState.newScreen(new MainScreen)
      }
    }
    override def render(canvas: Canvas): Unit = {}
  }

  class MainScreen extends GameScreen {

    override def name: String = "TestScreen"

    val Width = 480
    val Height = 320

    //val viewport = new Viewport(Window.width, Window.height/2)
    val viewport = new Viewport(Window.width, Window.height)
    viewport.setCamera(0, 0, Width, Height)
    viewport.scalingStrategy = Viewport.Fit

    private val characterBitmap = LoadingScreen.characterBitmap.get
    private val characterWidth = characterBitmap.width/4
    private val characterFrames = Array(
      BitmapRegion(characterBitmap, 0*characterWidth, 0, characterWidth, characterBitmap.height),
      BitmapRegion(characterBitmap, 1*characterWidth, 0, characterWidth, characterBitmap.height),
      BitmapRegion(characterBitmap, 2*characterWidth, 0, characterWidth, characterBitmap.height),
      BitmapRegion(characterBitmap, 3*characterWidth, 0, characterWidth, characterBitmap.height)
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
          val (wx, wy) = viewport.screenToWorld(x, y)
          this.x = wx
          this.y = wy
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
      canvas.drawRect(0, 0, Window.width, Window.height, Graphics.defaultPaint.withColor(Color.rgb(0, 0, 0)))
      viewport.withViewport(canvas){
        canvas.drawRect(0, 0, Window.width, Window.height, Graphics.defaultPaint.withColor(Color.rgb(0, 0, 200)))
        canvas.drawRect(0, 0, Width, Height, Graphics.defaultPaint.withColor(Color.rgb(204, 242, 204)))
        canvas.drawCircle(autoX.toInt, autoY.toInt, dp2px(50), Graphics.defaultPaint.withColor(Color.Black))

        canvas.drawBitmap(characterAnimation.currentFrame(totalTime), x.toInt, y.toInt, 1f, 0.5f)
      }
    }

  }

}
