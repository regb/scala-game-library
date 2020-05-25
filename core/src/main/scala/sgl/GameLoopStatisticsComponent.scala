package sgl

import util.metrics._

/** Statistics component to measure game loop performances.
  *
  * The game loop statistics is an optional component, that
  * can be mixed into the main application, and will hook into
  * the main loop to collect statistics. Being optional, it won't
  * impact performence on production build, if not included.
  */
trait GameLoopStatisticsComponent extends GameLoopComponent {
  this: GameStateComponent with InstrumentationProvider
  with GraphicsProvider with InputProvider with SystemProvider =>

  val statistics: GameLoopStatistics = new GameLoopStatistics

  override val gameLoopListener = new StatisticsGameLoopListener

  class StatisticsGameLoopListener extends GameLoopListener {

    private var initCalled = false

    private var beginTime = System.nanoTime
    override def onStepStart(): Unit = {
      if(!initCalled) {
        // Doesn't feel very good to do that, but we need a way to invoke the
        // init function of the Metrics before the first loop step, which would
        // be now...
        Metrics.init()
        initCalled = true
      }
      beginTime = System.nanoTime
    }
    override def onStepComplete(): Unit = {
      val elapsedTime: Long = System.nanoTime - beginTime
      statistics.completeFrame(elapsedTime)
      Metrics.update()
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

  /** Provides statistics about the running game.
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

    /** Histogram for game loop update calls, in ms.
      *
      * We use 15 buckets, from 0 to 15ms. Anything slower than
      * that for an update seems hopeless for any game as it would
      * prevent to reach 60 FPS and likely make it impossible to
      * even reach 30FPS given the need to also render.
      */
    private var updateHistogram: Histogram = null

    /** Histogram for game loop render calls, in ms. */
    private var renderHistogram: Histogram = null

    /** Histogram for game loop entire frame, in ms. */
    private var frameHistogram: Histogram = null

    private var fps: IntGauge = null


    // Must init lazily because of the cake dependencies (Metrics could be null).
    private def initIfNull(): Unit = {
      if(updateHistogram == null)
        updateHistogram = Metrics.HistogramWithLinearBuckets("/game_loop/update", 0f, 15f, 15)
      if(renderHistogram == null)
        renderHistogram = Metrics.HistogramWithLinearBuckets("/game_loop/render", 0f, 40f, 20)
      if(frameHistogram == null)
        frameHistogram = Metrics.HistogramWithLinearBuckets("/game_loop/frame", 0f, 40f, 20)
      if(fps == null)
        fps = Metrics.IntGauge("/game_loop/fps")
    }

    private[GameLoopStatisticsComponent] def completeUpdateFrame(dt: Long) = {
      initIfNull()
      updateHistogram.observe(dt/(1000f*1000f))
    }
    private[GameLoopStatisticsComponent] def completeRenderFrame(dt: Long) = {
      initIfNull()
      renderHistogram.observe(dt/(1000f*1000f))
    }

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
      initIfNull()
      frameHistogram.observe(dt/(1000f*1000f))

      currentSnapshotFrameCount += 1
      val currentTime = System.nanoTime
      if(currentTime - currentSnapshotStartTime > 1000*1000*1000) { //full snapshot taken
        fps.set(currentSnapshotFrameCount)
        currentSnapshotFrameCount = 0
        currentSnapshotStartTime = currentTime
      }
    }

  }

}
