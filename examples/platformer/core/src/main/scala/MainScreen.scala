package com.regblanc.sgl.platformer
package core

import sgl._
import geometry._
import scene._
import util._
import tiled._

trait MainScreenComponent extends ViewportComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider with AudioProvider
  with GameStateComponent with InputHelpersComponent with GameLoopStatisticsComponent
  with LoggingProvider with TiledMapRendererComponent with TmxJsonParserComponent =>

  import Graphics.{Bitmap, Canvas, Color}

  private implicit val LogTag = Logger.Tag("main-screen")

  class MainScreen extends GameScreen {

    override def name: String = "platformer-screen"

    val levelLoader: Loader[TiledMap] = System.loadText(ResourcesRoot / "levels" / "level.json").map(lvl => TmxJsonParser.parse(lvl.iterator))
    val tiledMapRendererLoader: Loader[TiledMapRenderer] = levelLoader.flatMap(lvl => TiledMapRenderer.load(lvl, ResourcesRoot / "levels"))
    addPreloading(levelLoader)
    addPreloading(tiledMapRendererLoader)


    private var level: TiledMap = _
    private var tiledMapRenderer: TiledMapRenderer = _
    private var viewport = new Viewport(Window.width, Window.height)
    override def onLoaded(): Unit = {
      level = levelLoader.value.get.get
      tiledMapRenderer = tiledMapRendererLoader.value.get.get
      viewport.setCamera(0, 0, level.totalWidth, level.totalHeight)
      viewport.scalingStrategy = Viewport.Fit
    }


    override def update(dt: Long): Unit = {
      InputHelpers.processEvents(e => e match {
        case _ => ()
      })

      //if(Inputs.Keyboard.left) {
      //  x -= dp2px(50)*(dt/1000f)
      //}
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawColor(Color.White)
      viewport.withViewport(canvas){
        tiledMapRenderer.render(canvas)
      }
    }

  }

}
