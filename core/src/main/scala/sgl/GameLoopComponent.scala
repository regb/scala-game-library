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
  self: GraphicsProvider with InputProvider with GameScreensComponent =>

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

    /*
     * Should be careful with milliseconds and nanoseconds. At the game level,
     * delta time for simulation should be at the millisecond step, but internally
     * to the loop for measuring delta, we use System.nanoTime, which returns
     * nanoseconds.
     */

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

    /** Updated by the loop to expose run statistics */
    var statistics: GameLoopStatistics = new GameLoopStatistics

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

      //the frame period is in milliseconds
      val FramePeriod: Option[Long] = Fps.map(fps => (1000.0 / fps.toDouble).toLong)

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

            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.clear()

            val newTime = System.nanoTime
            //delta time, in ms (all time measures are in nano)
            val dt = ((newTime - lastTime) / (1000*1000)).toLong
            lastTime = newTime

            /*
             * compute the vals before update as it could modify the screen stack
             */
            val currentScreen = screens.head
            val renderedScreens = screens.takeWhile(!_.isOpaque).reverse
            val lastOpaqueScreen = screens.find(_.isOpaque)

            val startProcessInputs = System.nanoTime
            currentScreen.processInputs(inputBuffer)
            inputBuffer.clearEvents()
            val processInputsTime = System.nanoTime - startProcessInputs
            statistics.completeProcessInputsFrame(processInputsTime)

            //TODO: should use nano time
            val startUpdate = System.nanoTime
            currentScreen.update(dt)
            val updateTime = System.nanoTime - startUpdate
            statistics.completeUpdateFrame(updateTime)

            val startRender = System.nanoTime
            lastOpaqueScreen.foreach(screen => screen.render(canvas))
            renderedScreens.foreach(screen => screen.render(canvas))
            val renderTime = System.nanoTime - startRender
            statistics.completeRenderFrame(renderTime)

          } finally {
            releaseScreenCanvas(canvas)
          }
        }



        val endTime: Long = System.nanoTime
        val elapsedTime: Long = endTime - beginTime

        statistics.completeFrame(elapsedTime)

        val sleepTime: Long = FramePeriod.map(fp => fp - elapsedTime/(1000*1000)).getOrElse(0)

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          //Log.w(LogTag, s"negative sleep time. frame period: $FramePeriod, elapsed time: $elapsedTime.")
        }
      }
    }

  }

  /** Provides statistics about the running game
    *
    * Is useful to undesrtand actual FPS, or the time taken
    * by each part of the game loop (processing inputs, update, rendering).
    *
    * This is always computed (no option to disable it) as it is unlikely
    * to be making any significant performence difference (a couple additions
    * on each frame). It's simpler architecture-wide to simply have it all
    * the time, although in the future we may want to separate it into a
    * different GameLoopComponent (such as InstrumentedGameLoopComponent) and
    * have the core game loop only used for production. But that does not seem
    * necessary as of today. Note that simply wrapping the statistics collect
    * into an if-then-else guarded by a debug flag is unlikely to help, as checking
    * the condition is probably as expensive as collecting the data (unless the conditions
    * can be removed at compile time).
    *
    * To avoid garbage collection, we use a mutable Statistics object, which
    * means that on each frame the same instance is updated. From a client
    * usage, you cannot store the statistics between frames to do some
    * comparisons.
    */
  class GameLoopStatistics {

    //all time measures are stored in nanoseconds, for precision.
    //this might be necessary as some update step would take less
    //than a full millisecond, and will thus lose precision. Values
    //such as average, in Double, are expressed in milliseconds.

    private var _totalTimeRunning: Long = 0
    /** total time the game loop has been running
      *
      * Ignore the time spent sleeping, so should not
      * be used to determine fps. Could be useful
      * to understand time spent by the application still
      * In nanoseconds.
      */
    def totalTimeRunning: Long = _totalTimeRunning

    private var _totalFrames: Long = 0
    def totalFrames: Long = _totalFrames

    private var _totalUpdateTime: Long = 0
    private var _totalRenderTime: Long = 0
    private var _totalProcessInputs: Long = 0

    //we compute current average using an exponential moving average.
    //the goal is to eventually eliminate outliers that would have appear, while still computing
    //some sort of average. We set alpha to 0.01, which should give results similar
    //to an average over the last 100 values (about 2-3 seconds, depending on FPS). 
    //Using this averaging technique, we don't need  to store any data points
    private var _updateTimeAverage: Double = 0
    private var _renderTimeAverage: Double = 0
    private var _processInputsTimeAverage: Double = 0
    private var _frameTimeAverage: Double = 0
    private val AverageAlpha: Double = 0.01

    def updateTimeAverage: Double = _updateTimeAverage/(1000*1000)
    def renderTimeAverage: Double = _renderTimeAverage/(1000*1000)
    def processInputsTimeAverage: Double = _processInputsTimeAverage/(1000*1000)
    def frameTimeAverage: Double = _frameTimeAverage/(1000*1000)

    private[GameLoopComponent] def completeUpdateFrame(dt: Long) = {
      _totalUpdateTime += dt
      _updateTimeAverage = AverageAlpha*dt + (1-AverageAlpha)*_updateTimeAverage
    }
    private[GameLoopComponent] def completeProcessInputsFrame(dt: Long) = {
      _totalProcessInputs += dt
      _processInputsTimeAverage = AverageAlpha*dt + (1-AverageAlpha)*_processInputsTimeAverage
    }
    private[GameLoopComponent] def completeRenderFrame(dt: Long) = {
      _totalRenderTime += dt
      _renderTimeAverage = AverageAlpha*dt + (1-AverageAlpha)*_renderTimeAverage
    }

    private var _measuredFps: Int = 0
    def measuredFps: Int = _measuredFps

    //interesting data such as FPS should be computed over the most recent
    //data and not as an average over the long term, so we keep computing
    //fps for the most recent snapshot. We store the real starting time and
    //compare with current system time, which is different from the actual
    //running time as it takes into account the sleeping time as well (runnin
    private var currentSnapshotStartTime: Long = System.nanoTime
    private var currentSnapshotFrameCount: Int = 0

    //we make the mutability only visible to the local implementation,
    //so that clients are not able to temper with the statistics
    private[GameLoopComponent] def completeFrame(dt: Long) = {
      _totalFrames += 1
      _totalTimeRunning += dt
      _frameTimeAverage = AverageAlpha*dt + (1-AverageAlpha)*_frameTimeAverage
      currentSnapshotFrameCount += 1
      val currentTime = System.nanoTime
      if(currentTime - currentSnapshotStartTime > 1000*1000*1000) {//full snapshot taken
        _measuredFps = currentSnapshotFrameCount
        currentSnapshotFrameCount = 0
        currentSnapshotStartTime = currentTime
      }
    }

  }

}
