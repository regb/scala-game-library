package sgl
package html5

import sgl.util._

import scala.scalajs.js
import org.scalajs.dom
import dom.html

trait Html5GameLoopProvider extends GameLoopProvider {
  self: Html5GraphicsProvider with GameStateComponent with LoggingProvider =>

  private implicit val Tag = Logger.Tag("game-loop")

  //no code automatically run here, it doesn't seem very safe to rely on Lifecycle to add code on
  //the framework (given that the exact call ordering is dubious) so I'd rather
  //have the code being started exactly at the right time: in main, after initializing everything
  //else. 
  //TODO: Probably means that we don't need the concept of a GameLoopProvider in the framework then


  def startGameLoop(): Unit = {

    val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)
    
    //only at startup, we add the starting screen to the game state
    gameState.newScreen(startingScreen)

    var lastTime: Long = js.Date.now.toLong
    def frameCode(): Unit = {
      val now = js.Date.now.toLong
      val dt: Long = now - lastTime

      if(FramePeriod.forall(delta => dt >= delta)) {
        lastTime = now

        val canvas = getScreenCanvas
        gameLoopStep(dt, canvas)
        releaseScreenCanvas(getScreenCanvas)
        
        FramePeriod.foreach(framePeriod => {
          //we check the time from the begininning of the frame until the end. The exact
          //dt sent to the update function will be different, as it depends on when the
          //setInterval fires the code, and might slightly overflow the target FPS
          val frameTime = js.Date.now.toLong - now
          if(frameTime > framePeriod)
            logger.warning("Frame took too long to execute. Losing FPS. Frame time: " + frameTime)
        })
      } else {
        logger.debug("Callback too early for Fps, skipping a frame")
      }

      if(requestAnimationFrameSupported)
        dom.window.requestAnimationFrame(dt => frameCode())
    }

    if(requestAnimationFrameSupported) {
      dom.window.requestAnimationFrame(dt => frameCode())
    } else {
      logger.warning("window.requestAnimationFrame not supported, fallback to setInterval for the game loop")
      dom.window.setInterval(() => frameCode(), FramePeriod.map(_.toDouble).getOrElse(10d))
    }

  }


  //TODO: maybe we need to stop this game loop if we support the pause when leaving browser tab. Not
  //      sure we should detect that anyway, as this is usually not the most reliable thing in the browser
}
