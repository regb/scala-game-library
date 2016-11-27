package com.regblanc.scalavator
package core

import sgl._
import scene._
import util._

trait LoadingScreenComponent {
  this: GraphicsProvider with InputProvider with GameLoopProvider
  with GameStateComponent with WindowProvider with SystemProvider
  with LoggingProvider with MainScreenComponent =>

  private implicit val Tag = Logger.Tag("loading-screen")

  object ResourcesLoadingManger {

    var character: Loader[Bitmap] = null
    var clouds: Loader[Bitmap] = null

  }

  class ScalavatorLoadingScreen[A](loaders: Seq[Loader[A]]) extends LoadingScreen[A](loaders) {

    override def render(canvas: Canvas): Unit = {
      logger.debug("Loading...")
    }

    //not the best way to access a promise, but at least
    //we try to hide the complexity within the loading
    //screen, meaning that the MainScreen just works with
    //fully loaded bitmap
    override def nextScreen: GameScreen = new MainScreen(
      ResourcesLoadingManger.character.value.get.get,
      ResourcesLoadingManger.clouds.value.get.get
    )
  }

  //we need to start loader only now, because it is risky to start
  //everything when the whole cake is initializing, as the framework
  //might not be quite ready yet
  override def startingScreen: GameScreen = {
    val pathPrefix = System.ResourcesPrefix / "drawable"
    ResourcesLoadingManger.character = Graphics.loadImage(pathPrefix / "character.png")
    ResourcesLoadingManger.clouds = Graphics.loadImage(pathPrefix / "clouds.png")
    val allResources = Array(
      ResourcesLoadingManger.character,
      ResourcesLoadingManger.clouds
    )
    new ScalavatorLoadingScreen(allResources)
  }


}
