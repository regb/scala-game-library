//package sgl
//
//import util._
//
///** Component to handle the main loop of the game
//  *
//  * The game main loop is handled in this component.
//  * Default implementation uses an additional thread on
//  * resume/pause to run the loop, but it might not be
//  * the best solution with the javascript backend (we'll revisit
//  * this when we actually have it).
//  *
//  * The idea is that the game loop component should handle its lifecycle
//  * via its Lifecycle methods, it would stop running the game loop during
//  * a pause, and restart it on a resume.
//  *
//  * We should probably try to extract this code from the core package,
//  * since it will not work with some backend such as scalajs (no thread).
//  */
//trait ThreadBasedGameLoopProvider extends GameLoopProvider with Lifecycle {
//  self: GraphicsProvider with GameStateComponent with LoggingProvider =>
//
//  private implicit val Tag = Logger.Tag("game-loop")
//
//  abstract override def resume(): Unit = {
//    val t = new Thread(gameLoop)
//    gameLoop.runningThread = t
//    t.start()
//  }
//
//  abstract override def pause(): Unit = {
//    gameLoop.stop()
//    gameLoop.runningThread = null
//    super.pause()
//  }
//
//  val gameLoop: GameLoop = new GameLoop
//
//
//}
