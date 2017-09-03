package sgl

/** Statistics component to measure game loop performances
  *
  * The game loop statistics is an optional component, that
  * can be mixed into the main application, and will hook into
  * the main loop to collect statistics. Being optional, it won't
  * impact performence on production build, if not included.
  */
trait GameLoopStatisticsComponent {
  this: GameLoopComponent =>

  val statistics: GameLoopStatistics = new GameLoopStatistics

  override val gameLoopListener = new StatisticsGameLoopListener

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
