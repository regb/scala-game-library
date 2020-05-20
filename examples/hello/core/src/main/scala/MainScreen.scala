package com.regblanc.sgl.test
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent extends ViewportComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider with AudioProvider
  with GameStateComponent with InputHelpersComponent with LoggingProvider with SchedulerProvider =>

  import Graphics.{Bitmap, Canvas, Color, BitmapRegion, Animation, RichCanvas}
  import Audio.{Music, Sound}
  import Window.dp2px

  private implicit val LogTag = Logger.Tag("main-screen")

  object LoadingScreen extends GameScreen {
    override def name: String = "Loading Screen"

    var characterBitmap: Option[Bitmap] = None
    var music: Option[Music] = None
    var beep: Sound = _
    var beepInfinite: Sound = _

    private var characterBitmapLoader: Loader[Bitmap] = null
    private var musicLoader: Loader[Music] = null
    private var beepLoader: Loader[Sound] = null

    override def update(dt: Long): Unit = {
      if(characterBitmapLoader == null) {
        characterBitmapLoader = Graphics.loadImage(ResourcesRoot / "drawable" / "character.png")
      }
      if(musicLoader == null) {
        //musicLoader = Audio.loadMusic(ResourcesRoot / "audio" / "music.wav")
        musicLoader = Audio.loadMusic(ResourcesRoot / "audio" / "music.ogg")
      }
      if(beepLoader == null) {
        beepLoader = Audio.loadSound(ResourcesRoot / "audio" / "beep.wav")
      }
      if(characterBitmapLoader.isLoaded && musicLoader.isLoaded && beepLoader.isLoaded) {
        characterBitmap = Some(characterBitmapLoader.value.get.get)
        music = Some(musicLoader.value.get.get)
        music.foreach(m => {
          m.setLooping(true)
          m.setVolume(0.5f)
          m.play()
        })
        val tmpBeep = beepLoader.value.get.get
        beep = tmpBeep.withConfig(2, 2f)
        beepInfinite = tmpBeep.looped(-1)
        // If we don't plan to use the original sound (if we only used the various
        // configs), we should dispose it.
        tmpBeep.dispose() 
        gameState.newScreen(new MainScreen)
      }
    }
    override def render(canvas: Canvas): Unit = {}
  }

  class MainScreen extends GameScreen {

    for(i <- 0 to 10000) {
      Scheduler.schedule(new HelloChunkedTask(i))
    }

    override def name: String = "TestScreen"

    val Width = 480
    val Height = 320

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

    var x = 0f
    var y = 0f

    var autoX = 0f
    var autoY = 0f

    private val beepInfinite = LoadingScreen.beepInfinite
    private var playingLoop: Option[beepInfinite.PlayedSound] = None

    var totalTime: Long = 0
    override def update(dt: Long): Unit = {
      InputHelpers.processEvents(e => e match {
        case Input.PointerDownEvent(x, y, _) =>
          LoadingScreen.beep.play()
          val (wx, wy) = viewport.screenToWorld(x, y)
          this.x = wx
          this.y = wy
        case Input.KeyDownEvent(Input.Keys.L) =>
          playingLoop match {
            case Some(s) =>
              beepInfinite.stop(s)
              playingLoop = None
            case None =>
              playingLoop = beepInfinite.play()
          }
        case _ => ()
      })
      totalTime += dt

      if(Inputs.Keyboard.left) {
        x -= dp2px(50)*(dt/1000f)
      }
      if(Inputs.Keyboard.right) {
        x += dp2px(50)*(dt/1000f)
      }

      if(Inputs.Keyboard.up) {
        y -= dp2px(50)*(dt/1000f)
      }
      if(Inputs.Keyboard.down) {
        y += dp2px(50)*(dt/1000f)
      }

      autoX += dp2px(50)*(dt/1000f)
      autoY += dp2px(50)*(dt/1000f)
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, Window.width, Window.height, Graphics.defaultPaint.withColor(Color.rgb(0, 0, 0)))
      viewport.withViewport(canvas){
        canvas.drawRect(0, 0, Window.width, Window.height, Graphics.defaultPaint.withColor(Color.rgb(0, 0, 200)))
        canvas.drawRect(0, 0, Width, Height, Graphics.defaultPaint.withColor(Color.rgb(204, 242, 204)))
        canvas.drawCircle(autoX, autoY, dp2px(50), Graphics.defaultPaint.withColor(Color.Black))

        canvas.drawBitmap(characterAnimation.currentFrame(totalTime), x, y, 1f, 0.5f)

        var rectWidth: Float = dp2px(50f)
        canvas.translate(dp2px(200f), dp2px(10f))
        canvas.drawRect(0, 0, rectWidth, rectWidth, Graphics.defaultPaint.withColor(Color.Red))
        canvas.translate(rectWidth + dp2px(10f), 0)
        canvas.withSave {
          canvas.scale(4f, 4f)
          canvas.drawRect(0, 0, rectWidth/4, rectWidth/4, Graphics.defaultPaint.withColor(Color.Red))
        }
        canvas.translate(rectWidth + dp2px(10f), 0)
        canvas.withSave {
          canvas.scale(8f, 8f)
          canvas.drawRect(0, 0, rectWidth/8, rectWidth/8, Graphics.defaultPaint.withColor(Color.Red))
        }
      }
    }

  }

  class HelloChunkedTask(i: Int) extends ChunkedTask {

    override val name = "hello-chunked-task"

    override protected def run(ms: Long): ChunkedTask.Status = {
      logger.info("Running task: " + i)
      ChunkedTask.Completed
    }
  }

}
