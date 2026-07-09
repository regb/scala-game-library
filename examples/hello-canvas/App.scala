package sgl.examples.hello.canvas
package core

import _root_.sgl._
import _root_.sgl.util._

trait AbstractApp extends Application with AssetsProvider {
  this: CanvasProvider with AudioProvider with WindowProvider with SystemProvider with SchedulerProvider =>

  import Graphics.{Animation, Bitmap, BitmapRegion, Canvas, Color}
  import Audio.{Music, Sound}

  private var music: Option[Music] = None
  private var beep: Sound = _
  private var beepInfinite: Sound = _

  private var characterBitmapLoader: Loader[Bitmap] = _
  private var musicLoader: Loader[Music] = _
  private var beepLoader: Loader[Sound] = _

  private var loaded = false
  private var elapsedSeconds = 0.0
  private var x = 0f
  private var y = 0f
  private var autoX = 0f
  private var autoY = 0f
  private var characterAnimation: Animation[BitmapRegion] = _

  private val Width = 480f
  private val Height = 320f

  override def create(): Unit = {
    for(_ <- 0 to 10) Scheduler.schedule(new HelloChunkedTask)
    Input.setInputProcessor(new InputProcessor with PointerInputProcessor {
      override def pointerDown(px: Int, py: Int, p: Int, b: Input.MouseButtons.MouseButton): Boolean = {
        if(loaded) beep.play()
        val (scale, offsetX, offsetY) = contentTransform
        x = clamp((px.toFloat - offsetX) / scale, 0f, Width)
        y = clamp((py.toFloat - offsetY) / scale, 0f, Height)
        true
      }

      override def keyDown(key: Input.Keys.Key): Boolean = {
        if(loaded && key == Input.Keys.L) beepInfinite.play()
        if(key == Input.Keys.M) music.foreach(_.stop())
        true
      }
    })
  }

  private def ensureLoadingStarted(): Unit = {
    if(characterBitmapLoader == null) characterBitmapLoader = Graphics.loadImage(Assets.drawable.character)
    if(musicLoader == null) musicLoader = Audio.loadMusic(Assets.audio.music)
    if(beepLoader == null) beepLoader = Audio.loadSound(Assets.audio.beep)
  }

  private def finishLoading(): Unit = {
    val bitmap = characterBitmapLoader.value.get.get
    music = Some(musicLoader.value.get.get)
    music.foreach { m =>
      m.setLooping(true)
      m.setVolume(0.5f)
      m.play()
    }
    val tmpBeep = beepLoader.value.get.get
    beep = tmpBeep.withConfig(2, 2f)
    beepInfinite = tmpBeep.looped(-1)
    tmpBeep.dispose()

    val characterWidth = bitmap.width / 4
    val frames = Array(
      BitmapRegion(bitmap, 0 * characterWidth, 0, characterWidth, bitmap.height),
      BitmapRegion(bitmap, 1 * characterWidth, 0, characterWidth, bitmap.height),
      BitmapRegion(bitmap, 2 * characterWidth, 0, characterWidth, bitmap.height),
      BitmapRegion(bitmap, 3 * characterWidth, 0, characterWidth, bitmap.height)
    )
    characterAnimation = new Animation(200, frames, Animation.Loop)
    loaded = true
  }

  private def update(dt: Double): Unit = {
    if(!loaded) {
      ensureLoadingStarted()
      if(characterBitmapLoader.isLoaded && musicLoader.isLoaded && beepLoader.isLoaded) finishLoading()
      return
    }

    elapsedSeconds += dt
    val movement = 50f * dt.toFloat
    if(Input.isKeyPressed(Input.Keys.Left)) x -= movement
    if(Input.isKeyPressed(Input.Keys.Right)) x += movement
    if(Input.isKeyPressed(Input.Keys.Up)) y -= movement
    if(Input.isKeyPressed(Input.Keys.Down)) y += movement
    autoX += movement
    autoY += movement
  }

  private def contentTransform: (Float, Float, Float) = {
    val scale = scala.math.min(Window.width.toFloat / Width, Window.height.toFloat / Height).toFloat
    val offsetX = (Window.width.toFloat - Width * scale) / 2f
    val offsetY = (Window.height.toFloat - Height * scale) / 2f
    (scale, offsetX, offsetY)
  }

  private def clamp(value: Float, min: Float, max: Float): Float =
    scala.math.max(min, scala.math.min(max, value)).toFloat

  private def render(canvas: Canvas): Unit = {
    canvas.drawRect(0, 0, Window.width.toFloat, Window.height.toFloat, Graphics.defaultPaint.withColor(Color.rgb(0, 0, 0)))
    if(!loaded) return

    canvas.withSave {
      val (scale, offsetX, offsetY) = contentTransform
      canvas.translate(offsetX, offsetY)
      canvas.scale(scale, scale)
      canvas.drawRect(0, 0, Width, Height, Graphics.defaultPaint.withColor(Color.rgb(204, 242, 204)))
      canvas.drawCircle(autoX, autoY, 50f, Graphics.defaultPaint.withColor(Color.Black))
      val animationTimeMillis = (elapsedSeconds * 1000.0).toLong
      canvas.drawBitmap(characterAnimation.currentFrame(animationTimeMillis), x, y, 160f / Window.logicalPpi, 0.5f)

      val rectWidth: Float = 50f
      canvas.translate(200f, 10f)
      canvas.drawRect(0, 0, rectWidth, rectWidth, Graphics.defaultPaint.withColor(Color.Red))
      canvas.translate(rectWidth + 10f, 0)
      canvas.withSave {
        canvas.scale(4f, 4f)
        canvas.drawRect(0, 0, rectWidth / 4, rectWidth / 4, Graphics.defaultPaint.withColor(Color.Red))
      }
      canvas.translate(rectWidth + 10f, 0)
      canvas.withSave {
        canvas.scale(8f, 8f)
        canvas.drawRect(0, 0, rectWidth / 8, rectWidth / 8, Graphics.defaultPaint.withColor(Color.Red))
      }
    }
  }

  override def frame(dt: Double): Unit = {
    update(dt)
    withFrameCanvas(render)
  }

  class HelloChunkedTask extends ChunkedTask {
    override val name = "hello-chunked-task"
    override protected def run(ms: Long): ChunkedTask.Status = ChunkedTask.Completed
  }
}
