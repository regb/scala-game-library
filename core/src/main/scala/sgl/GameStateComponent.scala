package sgl

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
  
    /** Render current game state on the Canvas
      *
      * You should not clean the canvas as it could contain elements
      * from other GameScreen on the game state, and in general will
      * be automatically cleaned up by the framework.
      */
    def render(canvas: Canvas): Unit
  
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
