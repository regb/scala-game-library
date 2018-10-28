package sgl

import util._

trait GameStateComponent {
  this: GraphicsProvider =>

  abstract class GameScreen {

    /** A title that summarizes the screen
      *
      * Used for analytics and debugging purposes.
      * It could vary from screen instance to screen instance,
      * for example by including a level.
      */
    def name: String
  
    //TODO: is it the right choice to use Long to represent
    //      delta? That seems like the best representation
    //      since millisecond are integers (unless we want to represent
    //      sub-millis?) but then most operations seem to need
    //      a conversion to double anyway. Besides, since it's always
    //      a delta time, Long is too much precision, so maybe using
    //      double + representing fraction of millis for increased
    //      simulation precision could be nice?

    /** update the game logic and physics
      *
      * The delta time dt is in millisecond. It represents the
      * elapsed time since the last call to update.
      * You should think of this function as the entry point to
      * update your game world to catch up with dt milliseconds
      * of simulation.
      */
    def update(dt: Long): Unit
  
    /** Render current game state on the Canvas.
      *
      * You should not clean the canvas as it could contain elements
      * from other GameScreen on the game state, and in general will
      * be automatically cleaned up by the framework.
      */
    def render(canvas: Graphics.Canvas): Unit

    /** Render the screen state while still loading.
      *
      * While the screen is loading (if some Loaders are set at creation time).
      * Then this loadingRender method is called instead of the main render method.
      * This gives the chance to the screen to still render something while waiting
      * on must-have assets to start the main update/render loop.
      */
    def loadingRender(canvas: Graphics.Canvas): Unit = {}

    private[sgl] var preloaders: List[Loader[_]] = List()
    def addPreloading[A](l: Loader[A]) = preloaders ::= l

    private[sgl] var _isLoading = true

    def isLoading: Boolean = _isLoading

    def onLoaded(): Unit = {}
  
    /** Determine whether next screen on the stack should be rendered 
      *
      * It is mostly an optimization, as the current screen would override
      * underlying screens anyway, but it's better to avoid spending time
      * rendering them.
      */
    val isOpaque: Boolean = false
  
    /** Notify the screen that the environement just changed.
      *
      * Typically, happens when the screen dimensions are updated.
      * The screen needs to update its internal state to adapt to
      * a new environment
      *
      * We think it's better to have those kind of events explicitly
      * notified to the game screen. Alternatively, a screen could check
      * on each call to update/render, but that would be annoying to write,
      * with this, you can compute some positionning data at init time, then
      * only recompute them if refresh is being called.
      */
    def refresh(): Unit = {}
  
  }

  /** A GameScreen that only updates on a fixed timestep
    *
    * 
    */
  abstract class FixedTimestepGameScreen(val fixedDelta: Long) extends GameScreen {
    require(fixedDelta > 0)

    //TODO: handle the "spiral of death" problem in that class maybe?
    //      could provide a "def panic(): Unit" function that gets called
    //      when we need to call fixedUpdate more than a configurable maxUpdateSteps

    private var accumulatedDelta = 0l
    final override def update(dt: Long): Unit = {
      accumulatedDelta += dt

      while(accumulatedDelta >= fixedDelta) {
        accumulatedDelta -= fixedDelta
        fixedUpdate()
      }
    }

    //no dt parameter, as it is always the fixedDelta value
    def fixedUpdate(): Unit

  }

  /*
   * A LoadingScreen is a screen dedicated to loading assets. Typically the game would
   * show a progress bar and/or a splash screen with the game logo. This is intended
   * as a way to load a large amount of resources in memory, which could take several
   * seconds.
   *
   * By contrast, the built-in preload feature from each GameScreen is meant
   * as a very short "last minute" loading for the screen, and should be used to load
   * very few assets and that are truly unique to that level. One particular example would
   * be level data (map, tilemap) for a platformer/puzzle game, where we need to load the 
   * current level for the current screen. Such loading should be extermely light and
   * only take a few frames, so that we don't need to display anything.
   */
  abstract class LoadingScreen[A](val loaders: Seq[Loader[A]]) extends GameScreen {
    import scala.collection.mutable.HashSet

    private var loadingErrors: HashSet[Loader[A]] = new HashSet()

    /** Indicate whether a loading error happened
      *
      * You can display an error message in the render method
      * based on that property.
      */
    protected def loadingError: Boolean = loadingErrors.nonEmpty

    /** The list of loaders that failed to load.
      *
      * The value returned can be different for each call, as
      * more and more loaders are being processed and
      * completed/failed.
      */
    protected def failed: Seq[Loader[A]] = loadingErrors.toList

    private var _remaining: HashSet[Loader[A]] = new HashSet()
    _remaining ++= loaders

    protected def remaining: Seq[Loader[A]] = _remaining.toList
    protected def loadedSuccessfully: Seq[Loader[A]] = loaders.diff(_remaining.toList).diff(failed)

    /** Compute the percentage of loaded loaders.
      *
      * This does not distinguish between errors and successes, simply
      * indicates how many loaders have been loaded. It also
      * does not do anything smart for loader that are fetching
      * larger data, it's a simple ratio of numbers completed/total.
      */
    protected def percentageLoaded: Double = 1 - (_remaining.size.toDouble/loaders.size.toDouble)

    override def name: String = "Loading Screen"

    override def update(dt: Long): Unit = {
      for(loader <- _remaining.toSet[Loader[A]]) {
        if(loader.isLoaded) {
          _remaining.remove(loader)
          if(loader.value.get.isFailure) {
            loadingErrors += loader
          }
        }
      }
      if(_remaining.isEmpty && !loadingError) {
        gameState.newScreen(nextScreen)
      }
    }

    /** The screen to instantiate and set in the game state.
      *
      * The function acts as a factory, that will be invoked only
      * once and only when all the Loaders are fully loaded.
      * This means that if the function creates an instance of the
      * GameScreen, the constructor can rely on the fact that
      * loading is completed.
      *
      * The Screen will automatically invoke GameState.newScreen
      * with the nextScreen when all resources are loaded.
      */
    def nextScreen: GameScreen

  }

  //this is a trait that can be mixed in when creating a
  //game screen, and it ads some extra useful information. Entirely optional
  trait GameScreenWithTime extends GameScreen {
    var totalTime: Long = 0

    //cool to feature abstract override !
    //but I think it will not work as a with in the game screen definition
    //as the override def update in the actual game screen implementation is
    //likely going to be illegal
    abstract override def update(dt: Long): Unit = {
      totalTime += dt
      super.update(dt)
    }

  }

  /** Override to define the game starting screen */
  def startingScreen: GameScreen

  val gameState: GameState = new GameState
    //TODO: should figure out naming convention for constants that are overriden, or
    //      top levelish constants that are specified when mixing the cake together


  class GameState {
    private var screens: List[GameScreen] = List()

    def screensStack: List[GameScreen] = screens

    def pushScreen(screen: GameScreen): Unit = {
      screens ::= screen
    }
    def popScreen(): Unit = {
      screens = screens.tail
    }
    def newScreen(screen: GameScreen): Unit = {
      screens = List(screen)
    }
  }

}
