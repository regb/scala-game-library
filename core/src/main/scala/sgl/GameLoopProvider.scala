package sgl

import util._

/** Each backend needs to implement a game loop provider
  *
  * The game loop provider for a given backend is responsible to setup
  * the general structure of the game loop, and it will leverage the
  * platform where it is running. On HTML5 for example, the provider
  * would rely on the JavaScript main loop engine, and not do too much,
  * while on the desktop it will likely uses local thread to handle
  * the game loop itself.
  */
trait GameLoopProvider extends Lifecycle {
  self: GameStateComponent with GraphicsProvider =>


  /** Override if you want to target a different FPS.
      None means that the game will run at max FPS */
  val Fps: Option[Int] = Some(40)

  //the frame period is in milliseconds
  lazy val FramePeriod: Option[Long] = Fps.map(fps => (1000.0 / fps.toDouble).toLong)

  abstract override def startup(): Unit = {
    super.startup()
    gameState.newScreen(startingScreen)
  }


  /** Interface to hook into the game loop
    *
    * Can be used for some low level optimization
    * or monitoring. Typically, this is how we
    * implement the statistics module to collect
    * measures
    */
  abstract class GameLoopListener {
    def onStart(): Unit = {}
      def onStepStart(): Unit = {}
        def onUpdateStart(): Unit = {}
        def onUpdateComplete(): Unit = {}
        def onRenderStart(): Unit = {}
        def onRenderComplete(): Unit = {}
      def onStepComplete(): Unit = {}
    def onComplete(): Unit = {}
  }
  def registerGameLoopListener(gameLoopListener: GameLoopListener): Unit = ???
  
  //this core loop step seems to be common to all platforms
  def gameLoopStep(dt: Long, canvas: Canvas): Unit = {
    //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    canvas.clear()

    /*
     * compute the vals before update as it could modify the screen stack
     */
    val currentScreen = gameState.screensStack.head
    val renderedScreens = gameState.screensStack.takeWhile(!_.isOpaque).reverse
    val lastOpaqueScreen = gameState.screensStack.find(_.isOpaque)

    currentScreen.update(dt)

    lastOpaqueScreen.foreach(screen => screen.render(canvas))
    renderedScreens.foreach(screen => screen.render(canvas))
  }

}


/** Statistics component to measure game loop performences
  *
  * The game loop statistics is an optional component, that
  * can be mixed into the main application, and will hook into
  * the main loop to collect statistics. Being optional, it won't
  * impact performence on production build, if not included.
  */
trait GameLoopStatisticsComponent extends Lifecycle {
  this: GameLoopProvider =>

  val statistics: GameLoopStatistics = new GameLoopStatistics

  override def startup(): Unit = {
    registerGameLoopListener(new StatisticsGameLoopListener)
  }

  class StatisticsGameLoopListener extends GameLoopListener {

    private var beginTime = System.nanoTime
    override def onStepStart(): Unit = {
      beginTime = System.nanoTime
    }
    override def onStepComplete(): Unit = {
      val elapsedTime: Long = System.nanoTime - beginTime
      statistics.completeFrame(elapsedTime)
    }

    private var startUpdate = System.nanoTime
    override def onUpdateStart(): Unit = {
      startUpdate = System.nanoTime
    }
    override def onUpdateComplete(): Unit = {
      val updateTime = System.nanoTime - startUpdate
      statistics.completeUpdateFrame(updateTime)
    }

    private var startRender = System.nanoTime
    override def onRenderStart(): Unit = {
      startRender = System.nanoTime
    }
    override def onRenderComplete(): Unit = {
      val renderTime = System.nanoTime - startRender
      statistics.completeRenderFrame(renderTime)
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

    //we compute current average using an exponential moving average.
    //the goal is to eventually eliminate outliers that would have appear, while still computing
    //some sort of average. We set alpha to 0.01, which should give results similar
    //to an average over the last 100 values (about 2-3 seconds, depending on FPS). 
    //Using this averaging technique, we don't need  to store any data points
    private var _updateTimeAverage: Double = 0
    private var _renderTimeAverage: Double = 0
    private var _frameTimeAverage: Double = 0
    private val AverageAlpha: Double = 0.01

    def updateTimeAverage: Double = _updateTimeAverage/(1000*1000)
    def renderTimeAverage: Double = _renderTimeAverage/(1000*1000)
    def frameTimeAverage: Double = _frameTimeAverage/(1000*1000)

    private[GameLoopStatisticsComponent] def completeUpdateFrame(dt: Long) = {
      _totalUpdateTime += dt
      _updateTimeAverage = AverageAlpha*dt + (1-AverageAlpha)*_updateTimeAverage
    }
    private[GameLoopStatisticsComponent] def completeRenderFrame(dt: Long) = {
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
    private[GameLoopStatisticsComponent] def completeFrame(dt: Long) = {
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
