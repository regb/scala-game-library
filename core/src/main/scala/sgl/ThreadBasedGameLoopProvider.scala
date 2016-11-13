package sgl

import util._

/** Component to handle the main loop of the game
  *
  * The game main loop is handled in this component.
  * Default implementation uses an additional thread on
  * resume/pause to run the loop, but it might not be
  * the best solution with the javascript backend (we'll revisit
  * this when we actually have it).
  *
  * The idea is that the game loop component should handle its lifecycle
  * via its Lifecycle methods, it would stop running the game loop during
  * a pause, and restart it on a resume.
  *
  * We should probably try to extract this code from the core package,
  * since it will not work with some backend such as scalajs (no thread).
  */
trait ThreadBasedGameLoopProvider extends GameLoopProvider with Lifecycle {
  self: GraphicsProvider with GameStateComponent with LoggingProvider =>

  private implicit val Tag = Logger.Tag("game-loop")

  abstract override def resume(): Unit = {
    val t = new Thread(gameLoop)
    gameLoop.runningThread = t
    t.start()
  }

  abstract override def pause(): Unit = {
    gameLoop.stop()
    gameLoop.runningThread = null
    super.pause()
  }

  val gameLoop: GameLoop = new GameLoop


  /** GameLoop Runnable that simulate the game
    *
    * The GameLoop contains the complete current state of the game
    * with the GameScreen stack and operation to push/pop screens.
    * Being a runnable means that the GameLoop can simply be passed
    * to a thread to start execution, or could also be run directly
    * by calling run method. There is a control mecanisms to stop the
    * loop in the run method, but this will not free the game state, as
    * all the game screens are part of the instance fields of the GameLoop.
    *
    * This means the GameLoop can be restarted in the same state later.
    * Very convenient when the system decides to pause the game and then
    * restart it, we just free the current Thread, and restart fresh with
    * a new one.
    */
  class GameLoop extends Runnable {

    /*
     * Should be careful with milliseconds and nanoseconds. At the game level,
     * delta time for simulation should be at the millisecond step, but internally
     * to the loop for measuring delta, we use System.nanoTime, which returns
     * nanoseconds.
     *
     * Not sure about the performence of measuring nanoseconds. I read a bit of
     * everything online, but I have to think that taking a nanoTime measurement
     * cannot seriously slow down the app. It also seems necessary to compute dt
     * using System.nanoTime, as System.currentTimeMillis might have a huge jump
     * of value due to the local clock getting updated concurrently.
     */

    /** Points to the thread currently running the game loop.
      *
      * when the GameLoop is running, the runningThread points
      * to the Thread that is executing the loop.
      */
    var runningThread: Thread = null
    private var running = true

    /** stop the game loop as soon as possible.
      *
      * Will likely terminate the current iteration, and
      * exit the game loop on the next frame. Safe to be called
      * more than once, second time will be ignored, unless the
      * game loop has been restarted (for example in a new Thread) in
      * between.
      */
    def stop(): Unit = synchronized {
      running = false
    }

    /** Init the game loop
      *
      * Only called once, while the run method will be
      * called several times potentially, so this is
      * the place to setup initial state
      */
    def init(): Unit = {
      //goes there, as it is not safe to do it in the constructor
      //because the startingScreen constructor might rely on some non
      //initialized stuff (GameLoop is constructed during cake initialization phase)
      //TODO: we might remove this need by creating factories for GameScreen instead of
      //      passing instances. Need to think of a system.
      gameState.newScreen(startingScreen)
    }

    private var firstCall = true

    override def run(): Unit = {
      if(firstCall) {
        //we use a first call variable instead of Lyfecycle, as it is not clear
        //that everything is properly initialised in the startup method
        init()
        firstCall = false
      }

      //the frame period is in milliseconds
      val period: Option[Long] = FramePeriod

      running = true

      /*
       * dt is the number of millisecond between the last update and
       * the current update. It is used to properly control the speed of
       * transitions of sprites.
       */
      var lastTime: Long = System.nanoTime

      while(running) {
        val beginTime: Long = System.nanoTime

        val canvas = getScreenCanvas

        if(canvas != null) {
          try {
            val newTime = System.nanoTime
            //delta time, in ms (all time measures are in nano)
            val dt = ((newTime - lastTime) / (1000*1000)).toLong
            lastTime = newTime

            gameLoopStep(dt, canvas)

          } finally {
            releaseScreenCanvas(canvas)
          }
        }

        val endTime: Long = System.nanoTime
        val elapsedTime: Long = endTime - beginTime

        val sleepTime: Long = FramePeriod.map(fp => fp - elapsedTime/(1000*1000)).getOrElse(0)

        //logger.trace(s"Measured FPS: ${statistics.measuredFps}")

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          logger.warning(s"negative sleep time. frame period: $FramePeriod, elapsed time: $elapsedTime.")
        }
      }
    }

  }

}
