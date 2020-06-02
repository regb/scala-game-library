package sgl
package html5

import sgl.util._
import util._
import themes._

import scala.scalajs.js
import js.annotation.JSExport
import org.scalajs.dom
import dom.html

trait Html5App extends GameApp 
                  with Html5WindowProvider with Html5SystemProvider with Html5GraphicsProvider 
                  with Html5InputProvider with Html5AudioProvider with SingleThreadSchedulerProvider {

  val theme: Theme

  /** The ID attribute of the HTML5 canvas to use for the game.
    *
    * If the main function of the game is called, then the game will
    * automatically find the game canvas identified by this ID (using
    * a regular getElementById). The canvas will be used as the main
    * rendering area.
    */
  val GameCanvasID: String

  var htmlCanvas: html.Canvas = null

  def main(args: Array[String]): Unit = {
    run(js.Dynamic.global.document.getElementById(GameCanvasID).asInstanceOf[html.Canvas])
  }


  /* 
   * To take advantage of high density screens, we scale up
   * the canvas according to the density, but still respect
   * the CSS pixel size for the appearance of the canvas. The
   * game code will however have more pixels to work with, and
   * they can ensure similar physical appearance by using the
   * ppi information exported by the WindowProvider. The end result
   * is that the rendering of the game will be more crisp.
   *
   * Note that such a change has an impact on most of the backend, as
   * the inputs need to be translated into canvas coordinates, and
   * the loadImage need to select the best resource, or perform
   * some scaling at runtime.
   */
  def prepareCanvas(canvas: html.Canvas): Unit = {
    canvas.style.width = canvas.width + "px"
    canvas.style.height = canvas.height + "px"
    canvas.width = (dom.window.devicePixelRatio*canvas.width).toInt
    canvas.height = (dom.window.devicePixelRatio*canvas.height).toInt
  }
  @JSExport
  def run(canvas: html.Canvas): Unit = {

    theme.init(canvas)
    prepareCanvas(canvas)
    dom.window.onresize = (event: dom.Event) => {
      theme.onResize(canvas)
      // After a resize, the theme might reset the CSS width/height, so we
      // need to prepare the canvas again.
      prepareCanvas(canvas)
    }
    this.htmlCanvas = canvas

    registerInputListeners()
    startGameLoop()

    // The scheduler is run using the task queue and not within requestAnimationFrame.
    // This is to keep the requestAnimationFrame code consistent in speed and focused
    // on simulating and rendering the game loop just before the refresh of the page.
    // This also lets us use more of the available free time between frames to perform
    // background tasks in the scheduler.
    //
    // We try to schedule as often as possible because apparently the browser does its
    // own throttling to around 4ms. If the queue is empty, we can schedule with our
    // own throttling.
    def runScheduler(): Unit = {
      if(Scheduler.run(5l)) {
        // No more tasks, so we can set the next timeout a bit later.
        dom.window.setTimeout(() => runScheduler(), 20l)
      } else {
        // More work to do, schedule as soon as possible.
        dom.window.setTimeout(() => runScheduler(), 0)
      }
    }
    dom.window.setTimeout(() => runScheduler, 50l)

    lifecycleListener.startup()
    lifecycleListener.resume()
  }

  private implicit val Tag = Logger.Tag("game-loop")

  override val MaxLoopStepDelta = Some(1000)

  def startGameLoop(): Unit = {

    val canvas: Graphics.Canvas = Graphics.Html5Canvas(htmlCanvas)

    val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)
    
    //only at startup, we add the starting screen to the game state
    gameState.newScreen(startingScreen)

    var lastTime: Option[Double] = None
    def frameCode(now: Double): Unit = {
      // TODO: The problem with requestAnimationFrame is that it will stop firing
      // when the user changes focus (new tab). That's good or bad depending on what
      // behavior you want, but that means autopause from a player's perspective.
      // Two things to address still.
      //   1) When the player comes back, the current time will have a huge jump in the future, so
      //      we need to guard against that, probably by capping the step to some max acceptable
      //      time. This can be handled with the MaxLoopStepDelta settings.
      //   2) We should handle the lifecycle methods properly, and we should offer a settings to
      //      not pause the game when we switch tabs (some games may want to keep playing, like
      //      simulation/tower defense/multiplayer). We could implement that by falling back on
      //      setInterval during the time away from the tab.
      val dt: Double = now - lastTime.getOrElse(now)
      lastTime = Some(now)
      gameLoopStep(dt.toLong, canvas)
      if(requestAnimationFrameSupported)
        dom.window.requestAnimationFrame(t => frameCode(t))
    }

    if(requestAnimationFrameSupported) {
      dom.window.requestAnimationFrame(t => frameCode(t))
    } else {
      logger.warning("window.requestAnimationFrame not supported, fallback to setInterval for the game loop")
      dom.window.setInterval(() => frameCode(js.Date.now), targetFramePeriod.map(_.toDouble).getOrElse(1000d/30d))
    }

  }

  //TODO: maybe we need to stop this game loop if we support the pause when leaving browser tab. Not
  //      sure we should detect that anyway, as this is usually not the most reliable thing in the browser

}
