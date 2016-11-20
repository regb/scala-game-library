package sgl
package html5

import util._

import scala.scalajs.js
import org.scalajs.dom
import dom.html

trait Html5GameLoopProvider extends GameLoopProvider {
  self: Html5GraphicsProvider with GameStateComponent =>

  //no code automatically run here, it doesn't seem very safe to rely on Lifecycle to add code on
  //the framework (given that the exact call ordering is dubious) so I'd rather
  //have the code being started exactly at the right time: in main, after initializing everything
  //else. 
  //TODO: Probably means that we don't need the concept of a GameLoopProvider in the framework then

  def startGameLoop(): Unit = {
    //TODO: use requestAnimationFrame
    //TODO: actually use the FPS
    
    //only at startup, we add the starting screen to the game state
    gameState.newScreen(startingScreen)

    var lastTime: Long = js.Date.now.toLong
    dom.window.setInterval(() => {

      val now = js.Date.now.toLong
      val dt: Long = now - lastTime
      lastTime = now

      val canvas = getScreenCanvas
      gameLoopStep(dt, canvas)
      releaseScreenCanvas(getScreenCanvas)

    }, 40)
  }

  //TODO: maybe we need to stop this game loop if we support the pause when leaving browser tab. Not
  //      sure we should detect that anyway, as this is usually not the most reliable thing in the browser
}
