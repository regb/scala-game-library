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

  @JSExport
  def run(canvas: html.Canvas): Unit = {

    theme.init(canvas)

    this.htmlCanvas = canvas

    registerInputListeners()
    startGameLoop()

    lifecycleListener.startup()
    lifecycleListener.resume()
  }


  private implicit val Tag = Logger.Tag("game-loop")

  def startGameLoop(): Unit = {

    val canvas: Graphics.Canvas = Graphics.Html5Canvas(htmlCanvas)

    val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)
    
    //only at startup, we add the starting screen to the game state
    gameState.newScreen(startingScreen)

    var lastTime: Long = js.Date.now.toLong
    def frameCode(): Unit = {
      val now = js.Date.now.toLong
      //we cap at 1 sec, because requestAnimationFrame stops callback when
      //switching tabs, essentially pausing the game. Also in general, doing
      //giant update steps is extremely dangerous, but it would be nice to
      //control this in a better way
      val dt: Long = (now - lastTime) min 1000

      if(targetFramePeriod.forall(delta => dt >= delta)) {
        lastTime = now

        gameLoopStep(dt, canvas)

        // TODO: take into account available time.
        Scheduler.run(10l)
        
        targetFramePeriod.foreach(framePeriod => {
          //we check the time from the begininning of the frame until the end. The exact
          //dt sent to the update function will be different, as it depends on when the
          //setInterval fires the code, and might slightly overflow the target FPS
          val frameTime = js.Date.now.toLong - now
          logger.info("frame time: " + frameTime)
          logger.info("frame period: " + framePeriod)
          if(frameTime > framePeriod)
            logger.warning("Frame took too long to execute. Losing FPS. Frame time: " + frameTime)
        })
      } else {
        logger.debug("GameLoop callback invoked before enough time has elapsed. No need to perform any work, just waiting for next callback.")
      }

      if(requestAnimationFrameSupported)
        dom.window.requestAnimationFrame(dt => frameCode())
    }

    if(requestAnimationFrameSupported) {
      dom.window.requestAnimationFrame(dt => frameCode())
    } else {
      logger.warning("window.requestAnimationFrame not supported, fallback to setInterval for the game loop")
      dom.window.setInterval(() => frameCode(), targetFramePeriod.map(_.toDouble).getOrElse(10d))
    }

  }

  //TODO: maybe we need to stop this game loop if we support the pause when leaving browser tab. Not
  //      sure we should detect that anyway, as this is usually not the most reliable thing in the browser

}
