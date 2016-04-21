package sgl

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
  */
trait GameLoopComponent extends Lifecycle {
  this: GraphicsProvider with InputProvider with GameScreensComponent =>

  /*
   * TODO: maybe this should be named GameState (should export transition
   *       between GameScreen).
   */

  /** Override if you want to target a different FPS.
      None means that the game will run at max FPS */
  val Fps: Option[Int] = Some(40)
  
  /** Override to define the game starting screen */
  def startingScreen: GameScreen

  abstract override def startup(): Unit = {
    println("startup GameLoopProvider")
    super.startup()
  }

  abstract override def resume(): Unit = {
    println("resuming game loop")
    val t = new Thread(gameLoop)
    gameLoop.runningThread = t
    t.start()
  }

  abstract override def pause(): Unit = {
    println("pausing the game loop")
    gameLoop.stop()
    gameLoop.runningThread = null
    super.pause()
  }

  abstract override def shutdown(): Unit = {
    println("shutdown down game loop")
    super.shutdown()
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

    /** Points to the thread currently running the game loop.
      *
      * when the GameLoop is running, the runningThread points
      * to the Thread that is executing the loop.
      */
    var runningThread: Thread = null

    private var screens: List[GameScreen] = List()

    def pushScreen(screen: GameScreen): Unit = {
      screens ::= screen
    }
    def popScreen(): Unit = {
      screens = screens.tail
    }
    def newScreen(screen: GameScreen): Unit = {
      screens = List(screen)
    }


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

    /** Updated by the loop to expose the fps */
    var measuredFps: Int = 0

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
      newScreen(startingScreen)
    }

    private var firstCall = true

    override def run(): Unit = {
      if(firstCall) {
        //we use a first call variable instead of Lyfecycle, as it is not clear
        //that everything is properly initialised in the startup method
        init()
        firstCall = false
      }

      val FramePeriod: Option[Int] = Fps.map(fps => (1000.0 / fps.toDouble).toInt)
      var ticks: Long = 0

      running = true

      /*
       * dt is the number of millisecond between the last update and
       * the current update. It is used to properly control the speed of
       * transitions of sprites.
       */
      var lastTime: Long = System.currentTimeMillis
      var dt: Int = 0

      /*
       * These variables are used to measure (observe) the actual
       * FPS.
       */
      var fpsSnapshotStart: Long = System.currentTimeMillis
      var fpsSnapshotFrameCount: Int = 0

      while(running) {
        ticks += 1

        val beginTime: Long = System.currentTimeMillis

        val canvas = getScreenCanvas

        if(canvas != null) {
          try {

            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.clear()

            val newTime = System.currentTimeMillis
            dt = (newTime - lastTime).toInt
            lastTime = newTime

            /*
             * compute the vals before update as it could modify the screen stack
             */
            val currentScreen = screens.head
            val renderedScreens = screens.takeWhile(!_.isOpaque).reverse
            val lastOpaqueScreen = screens.find(_.isOpaque)

            currentScreen.processInputs(inputBuffer)
            inputBuffer.clearEvents()

            currentScreen.update(dt)

            lastOpaqueScreen.foreach(screen => screen.render(canvas))
            renderedScreens.foreach(screen => screen.render(canvas))

          } finally {
            releaseScreenCanvas(canvas)
          }
        }



        val endTime: Long = System.currentTimeMillis
        val elapsedTime: Long = endTime - beginTime

        if(endTime - fpsSnapshotStart > 1000) {//full snapshot taken
          measuredFps = fpsSnapshotFrameCount
          fpsSnapshotFrameCount = 0
          fpsSnapshotStart = endTime
        } else {
          fpsSnapshotFrameCount += 1
        }

        val sleepTime: Long = FramePeriod.map(fp => fp - elapsedTime).getOrElse(0)


        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          //Log.w(LogTag, s"negative sleep time. frame period: $FramePeriod, elapsed time: $elapsedTime.")
        }
      }
    }

  }

}
