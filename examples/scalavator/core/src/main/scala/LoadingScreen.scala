package com.regblanc.scalavator
package core

import sgl._
import scene._
import util._

trait LoadingScreenComponent {
  this: GraphicsProvider with InputProvider with GameStateComponent
  with WindowProvider with SystemProvider with LoggingProvider with MainScreenComponent =>

  import Graphics._

  private implicit val Tag = Logger.Tag("loading-screen")

  var characterIdle: Loader[Bitmap] = null
  var characterPrejump: Loader[Bitmap] = null
  var characterJump: Loader[Bitmap] = null
  var bug: Loader[Bitmap] = null
  var clouds: Loader[Bitmap] = null

  class ScalavatorLoadingScreen[A](loaders: Seq[Loader[A]]) extends LoadingScreen[A](loaders) {

    override def render(canvas: Canvas): Unit = {
      logger.debug("Loading...")
    }

    //not the best way to access a promise, but at least
    //we try to hide the complexity within the loading
    //screen, meaning that the MainScreen just works with
    //fully loaded bitmap
    override def nextScreen: GameScreen = new MainScreen(
      characterIdle.value.get.get,
      characterPrejump.value.get.get,
      characterJump.value.get.get,
      bug.value.get.get,
      clouds.value.get.get
    )
  }

  //we need to start loader only now, because it is risky to start
  //everything when the whole cake is initializing, as the framework
  //might not be quite ready yet
  override def startingScreen: GameScreen = {
    val pathPrefix = ResourcesPrefix / "drawable"
    characterIdle = Graphics.loadImage(pathPrefix / "character_idle.png")
    characterPrejump = Graphics.loadImage(pathPrefix / "character_prejump.png")
    characterJump = Graphics.loadImage(pathPrefix / "character_jump.png")
    bug = Graphics.loadImage(pathPrefix / "bug.png")
    clouds = Graphics.loadImage(pathPrefix / "clouds.png")
    val allResources = Array(
      characterIdle,
      characterPrejump,
      characterJump,
      bug,
      clouds
    )
    new ScalavatorLoadingScreen(allResources)
  }


}
