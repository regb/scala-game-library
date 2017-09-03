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

  //the frame period is in milliseconds
  //lazy val FramePeriod: Option[Long] = Fps.map(fps => (1000.0 / fps.toDouble).toLong)

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

    //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    canvas.clear()

    /*
     * compute the vals before update as it could modify the screen stack
     */
    val currentScreen = gameState.screensStack.head
    val renderedScreens = gameState.screensStack.takeWhile(!_.isOpaque).reverse
    val lastOpaqueScreen = gameState.screensStack.find(_.isOpaque)

    gameLoopListener.onUpdateStart()
    currentScreen.update(dt)
    gameLoopListener.onUpdateComplete()

    gameLoopListener.onRenderStart()
    lastOpaqueScreen.foreach(screen => screen.render(canvas))
    renderedScreens.foreach(screen => screen.render(canvas))
    gameLoopListener.onRenderComplete()

    gameLoopListener.onStepComplete()
  }

}
