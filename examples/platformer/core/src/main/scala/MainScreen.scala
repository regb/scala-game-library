package com.regblanc.sgl.platformer
package core

import sgl._
import geometry._
import scene._
import util._
import sgl.util.metrics.InstrumentationProvider
import tiled._

trait MainScreenComponent extends ViewportComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider with AudioProvider
  with GameStateComponent with InputHelpersComponent with InstrumentationProvider
  with LoggingProvider with TiledMapRendererComponent with TmxJsonParserComponent =>

  import Graphics.{Bitmap, BitmapRegion, Canvas, Color, Animation}

  private implicit val LogTag = Logger.Tag("main-screen")

  private val dt: Long = 5l

  class MainScreen extends FixedTimestepGameScreen(dt) {

    override def name: String = "platformer-screen"

    private val playerPaint = Graphics.defaultPaint.withColor(Color.Blue)

    val levelLoader: Loader[TiledMap] = System.loadText(ResourcesRoot / "levels" / "level.json").map(lvl => TmxJsonParser.parse(lvl.iterator))
    val tiledMapRendererLoader: Loader[TiledMapRenderer] = levelLoader.flatMap(lvl => TiledMapRenderer.load(lvl, ResourcesRoot / "levels"))
    addPreloading(levelLoader)
    addPreloading(tiledMapRendererLoader)

    val playerLoader: Loader[Bitmap] = Graphics.loadImage(ResourcesRoot / "drawable" / "player.png")
    addPreloading(playerLoader)

    private var map: TiledMap = _
    private var tiledMapRenderer: TiledMapRenderer = _
    private var viewport = new Viewport(Window.width, Window.height)
    private var playerRect = Rect(0, 0, 0, 0)
    private var oldPlayerRect = Rect(0, 0, 0, 0)
    private var playerAnimation: Animation[BitmapRegion] = _
    private var goalEllipse = Ellipse(0, 0, 0, 0)
    private var solidCollisionLayers: Vector[TileLayer] = _
    override def onLoaded(): Unit = {
      map = levelLoader.value.get.get
      tiledMapRenderer = tiledMapRendererLoader.value.get.get
      viewport.setCamera(0, 0, map.totalWidth, map.totalHeight)
      viewport.scalingStrategy = Viewport.Fit
      val objectLayer = map.objectLayer("GameObjects")
      playerRect = objectLayer("player").asInstanceOf[TiledMapRect].rect
      oldPlayerRect = playerRect.clone
      playerAnimation = new Animation(200, BitmapRegion.split(playerLoader.value.get.get, 0, 0, 30, 60, 3, 1), Animation.Loop)
      goalEllipse = objectLayer("goal").asInstanceOf[TiledMapEllipse].ellipse
      solidCollisionLayers = map.tileLayers.filter(_.properties.find(_.name == "collision_type").flatMap(_.stringValue).exists(_ == "solid"))
    }

    private var totalTime: Long = 0
    override def fixedUpdate(): Unit = {
      totalTime += dt
      InputHelpers.processEvents(e => e match {
        case _ => ()
      })

      if(Inputs.Keyboard.left) {
        playerRect.left = playerRect.left - 0.15f*dt
      }
      if(Inputs.Keyboard.right) {
        playerRect.left = playerRect.left + 0.15f*dt
      }
      val collidingX = solidCollisionLayers.exists(tl => {
        tl.intersectingTiles(playerRect).exists(_.nonEmpty)
      })
      if(collidingX) {
        playerRect.left = oldPlayerRect.left
      } else {
        oldPlayerRect.left = playerRect.left
      }

      if(Inputs.Keyboard.up) {
        playerRect.top = playerRect.top - 0.15f*dt
      }
      if(Inputs.Keyboard.down) {
        playerRect.top = playerRect.top + 0.15f*dt
      }
      val collidingY = solidCollisionLayers.exists(tl => {
        tl.intersectingTiles(playerRect).exists(_.nonEmpty)
      })
      if(collidingY) {
        playerRect.top = oldPlayerRect.top
      } else {
        oldPlayerRect.top = playerRect.top
      }
    }

    private val BackgroundColor = Color.rgb(0, 255, 50)

    private val metricsPaint = Graphics.defaultPaint.withColor(Color.White).withFont(Graphics.Font.Monospace.withSize(20))

    override def render(canvas: Canvas): Unit = {
      canvas.drawColor(BackgroundColor)
      viewport.withViewport(canvas){
        tiledMapRenderer.render(canvas, totalTime)

        canvas.drawBitmap(playerAnimation.currentFrame(totalTime), playerRect.left, playerRect.top)
        canvas.drawOval(goalEllipse.x, goalEllipse.y, goalEllipse.width, goalEllipse.height, playerPaint)
      }

      Metrics.renderMetrics(canvas, metricsPaint)
    }

  }

}
