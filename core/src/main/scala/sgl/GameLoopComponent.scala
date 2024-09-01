package sgl

import util._

/** Definitions related to the Game Loop.
  *
  * Each backend needs to implement way to provide the game loop, respecting
  * settings and architecture from the game loop component.
  *
  * The game loop component for a given backend is responsible to setup
  * the general structure of the game loop, and it will leverage the
  * platform where it is running. On HTML5 for example, the provider
  * would rely on the JavaScript main loop engine, and not do too much,
  * while on the desktop it will likely uses a local thread to handle
  * the game loop itself.
  */
trait GameLoopComponent {
  self: GameStateComponent with GraphicsProvider =>

  /** Defines the target FPS of the game
    *
    * Override to define a target FPS.
    *
    * If value is None, the game will run at maximal possible
    * FPS depending on the platform. On Desktop, it would typically
    * not sleep between each frame, but on the web, it might set
    * an arbitrary speed for the setInterval.
    */
  val TargetFps: Option[Int] = Some(30)

  // Convert an FPS to its corresponding frame period in in milliseconds.
  def framePeriod(fps: Int): Long = (1000.0 / fps.toDouble).toLong

  /** Configure a maximum delta time (in ms) that the game loop will handle.
    *
    * If the actual delta time computed between two frames is larger
    * than this value, we will cap the delta time to this value. This
    * means that the update function will not get the correct delta time,
    * but instead a capped value.
    *
    * This can lead to problems because time magically disappear, but
    * usually that should just result in a big lag and then things
    * should recover, as long as you never rely on the real actual time
    * for other computations.
    *
    * This can be useful to avoid time jump that would not be well supported
    * by the physics simulation (jumps of thousands of seconds). On the
    * other hand, this should not be too low because you ideally do not
    * want to let time disappear, but instead you would accept a choppy
    * frame with a simulation update of few hundred ms in one step. The
    * smallest acceptable value for this is probably around 1 second, anything
    * lower than that is probably better handled by just simulating and
    * accepting the frame dropped, but not losing actual data.
    */
  val MaxLoopStepDelta: Option[Long] = None

  /** Interface to hook into the game loop
    *
    * Can be used for some low level optimization
    * or monitoring. This is how we
    * implement the statistics module to collect
    * measures
    */
  abstract class GameLoopListener {
    def onStepStart(): Unit = {}
      def onUpdateStart(): Unit = {}
      def onUpdateComplete(): Unit = {}
      def onRenderStart(): Unit = {}
      def onRenderComplete(): Unit = {}
    def onStepComplete(): Unit = {}
  }

  /** Default game loop listener, doing nothing
    *
    * Using this implementation as default, and if the
    * gameLoopListener is not overrident at wiring time,
    * this gives the opportunity to the compiler to
    * optimize the game loop step, by removing calls to the
    * listener. Thus, we get for free the flexibility of
    * having hooks ready for people that needs extra monitoring,
    * without code duplication, which simplifies maintenance.
    *
    * At least in Scala.js, the compiler takes advantage of knowing
    * all the information at compile time, and emit code for the gameLoopStep
    * function with no reference to the gameLoopListener.
    */
  object SilentGameLoopListener extends GameLoopListener {
    final override def onStepStart(): Unit = {}
      final override def onUpdateStart(): Unit = {}
      final override def onUpdateComplete(): Unit = {}
      final override def onRenderStart(): Unit = {}
      final override def onRenderComplete(): Unit = {}
    final override def onStepComplete(): Unit = {}
  }

  /** The game loop listener to hook into the game loop
    *
    * You can can override with any sort of GameLoopListener
    * implementation, that will perform operation at various
    * point in the game loop execution. Some common features are
    * implemented this way, such as sgl.GameLoopStatisticsComponent
    * which you can mix in when wiring the app.
    *
    * We should provide way to combine listeners together to accumulate
    * several behaviour as well.
    */
  val gameLoopListener: GameLoopListener = SilentGameLoopListener
  
  //this core loop step seems to be common to all platforms
  def gameLoopStep(dt: Long, canvas: Graphics.Canvas): Unit = {
    gameLoopListener.onStepStart()

    val rdt = MaxLoopStepDelta.map(m => dt min m).getOrElse(dt)

    //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    // TODO: should we clear the canvas? I don't think so, because I think
    //       the render call should have the choice to do it, and because
    //       when we skip the render due to setting a new screen stack, we
    //       get an ugly black frame of transition.
    //canvas.clear()

    // Save the screen stack first as it could get modified during an update.
    val screensStack = gameState.screensStack

    val currentScreen = gameState.screensStack.head

    if(currentScreen.isLoading && currentScreen.preloaders.exists((l: Loader[_]) => !l.isLoaded)) {
      currentScreen.loadingRender(canvas)
    } else {
      if(currentScreen.isLoading) {
        // current screen just finished loading
        currentScreen.onLoaded()
        currentScreen._isLoading = false
      }

      if(rdt > 0) {
        gameLoopListener.onUpdateStart()
        currentScreen.update(rdt)
        gameLoopListener.onUpdateComplete()
      }

      // If the update modified the screen stack, better to stop
      // now because the render can be tricky, and we want to
      // ensure the call to onLoaded is done on the new screen.
      if(screensStack == gameState.screensStack) {

        val transparentScreens: List[GameScreen] = gameState.screensStack.takeWhile(!_.isOpaque)
        val lastOpaqueScreen: Option[GameScreen] = gameState.screensStack.find(_.isOpaque)
        // Screens to render, in the order of the deepest more first.
        val renderedScreens = lastOpaqueScreen ++ transparentScreens.reverse

        gameLoopListener.onRenderStart()
        renderedScreens.foreach(_.render(canvas))
        gameLoopListener.onRenderComplete()
      }
    }

    gameLoopListener.onStepComplete()
  }

}
