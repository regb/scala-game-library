package sgl.android

import sgl.util._

import sgl.{android => adr, _}

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.util.AttributeSet
import _root_.android.view.SurfaceView
import _root_.android.view.SurfaceHolder
import _root_.android.view.WindowManager

/** Activity providing all providers implementation for Android.
  *
  * This abstract Activity implements basic lifecycle methods of Android
  * Activities and also mix-in all the cake providers from SGL.
  * The role of this is to initialize a SurfaceView as the root view of
  * the activity and start a game loop invoking the update/render functions,
  * rendering to the SurfaceView.
  */
trait AndroidApp extends Activity with GameApp
                    with AndroidGraphicsProvider with AndroidInputProvider with AndroidAudioProvider
                    with AndroidWindowProvider with ThreadPoolSchedulerProvider with AndroidSystemProvider
                    with GameStateComponent {

  this: LoggingProvider =>

  /** Control if the screen should always stay on.
    *
    * Control the flag FLAG_KEEP_SCREEN_ON, if set to true, as long as we are
    * within the game activity, the app will not go to sleep (probably what the
    * player expect). Active by default, override to change.
    */
  val KeepScreenOn: Boolean = true

  // TODO: Provide a config control for Portrait/Landscape/Locked, which is
  //  optional (if not used, do nothing, which means that the user can just
  //  configure its AndroidManifest). We should provide a way to give constraints
  //  (min width, max width) under which to force which mode. For example, maybe
  //  we force portrait for a phone (up to 500dp) but then we let portrait/landscape
  //  if screen is larger, but we still lock it in place (no rotation). These combination
  //  of constraints are impossible to specify in the AndroidManifest only, but we
  //  can ensure them programatically.

  // The following flags are used to track the application state. It
  // is not very clear what is the relationship between activity lifecycle
  // calls (onResume, etc) and Surface callback calls (onSurfaceCreated, etc).
  // Rather than assuming some events always happen after others, we track the
  // state with a few boolean flags, and we take decisions on when to run the
  // game loop based on this state.

  /** Indicates if the activity is in running state
    *
    * This checks that the activity is between calls
    * onResume and onPause. We use this in the SurfaceHolder.Callback
    * to check if we should fire up a resume event for the app.
    */
  var appResumed: Boolean = false

  /** Indicates if the surface is ready to be drawn on
    *
    * This is set after the SurfaceCreated callback and
    * means that we can start drawing on the surface. It
    * should be used in conjunction with the appResumed
    * flag to control when to run the game loop.
    */
  var surfaceReady: Boolean = false

  var gameLoop: GameLoop = null
  var gameLoopThread: Thread = null

  private implicit val LogTag: Logger.Tag = Logger.Tag("sgl-main-activity")

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    logger.trace("onCreated called")

    appResumed = false
    surfaceReady = false

    gameView = new GameView(null)
    setContentView(gameView)

    /* make sure that if the activity is launched again and the previous
     * one is still around, it will finish the new one and use the existing one
     */
    val intent = getIntent()
    if(!isTaskRoot() && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && 
                        Intent.ACTION_MAIN.equals(intent.getAction())) {
      logger.warning("Main Activity is not the root.  Finishing Main Activity instead of launching.")
      finish()
      // With this call to finish, it seem that onDestroy will be called before any other lifecycle methods (onResume, onPause).
      // In particular, we need to be careful with the Scheduler shutdown.
      // finish() does not stop the invokation of onCreate, but it will call onDestroy right after. So we do not return
      // in order to maintain the symmetry between onCreate and onDestroy.
    }

    this.registerInputsListeners()

    if(KeepScreenOn)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // lifecycle of SGL
    lifecycleListener.startup()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    Scheduler.shutdown()

    //lifecycle of SGL
    lifecycleListener.shutdown()
  }

  // Although we use flags for the various state, we should
  // still start the game loop in the onResume (and check other flags as part
  // of the game loop to know if we can actually render a frame or not). We
  // should use onResume and onPause to start/pause things such as game loops, worker
  // threads, and music. It is safe as we know that these callbacks will always be
  // invoked when the user loses focus for a significant amount of time.
  override def onResume(): Unit = {
    super.onResume()
    logger.trace("onResumed called")

    appResumed = true
  
    gameLoop = new GameLoop
    gameLoopThread = new Thread(gameLoop)
    gameLoopThread.start()
    Scheduler.resume()

    // TODO: maybe the lifecycle resume event should be more precise and take into account
    //       things like surfaceReady and focus flags.
    lifecycleListener.resume()
  }

  override def onPause(): Unit = {
    super.onPause()
    logger.trace("onPause called")

    appResumed = false

    gameLoop.running = false
    Scheduler.pause()

    lifecycleListener.pause()
  }


  // TODO: handle config changes in a way transparent to the framework.
  //       * The window size change should be already handled from the GameView surface
  //       * Orientation change should be notified somehow (isn't it just a height/width change?)
  //       * Locale for text translations
  //    The other thing to consider is that the Manifest should contain a configChanges attribute
  //    that must list all the config change that should be notified with this callback. Any config
  //    change that is not listed there will not be notified and instead trigger an Activity restart.
  //    As we certainly don't want an activity restart, it would be nice to automatically generate this
  //    configChanges attribute so that games don't need to worry about.
  // override def onConfigurationChange(newConfig: Configuration): Unit = {}

  class GameLoop extends Runnable {

    // This, as a side-effect, will shadow LogTag from the AndroidApp and ensures
    // logging calls in the GameLoop uses that tag instead.
    private implicit val LogTag = Logger.Tag("game-loop")

    private val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true

    override def run(): Unit = {
      var lastTime: Long = java.lang.System.nanoTime

      while(running) {
        val beginTime: Long = java.lang.System.nanoTime

        // We check surfaceReady here, as the game loop is started on the onResume
        // callback and the surface is not necessarily ready yet. The rest of the loop
        // is safe to perform and will just sleep until the surface is actually ready.
        if(surfaceReady) {

          if(gameState.screensStack.isEmpty) {
            // This is the first time we run here and there's no game state yet, so
            // let's initialize it.
            // It's important to initialize the startingScreen only once all the Android
            // system dependencies are ready (most notably, the surface).
            gameState.newScreen(startingScreen)
          }

          val androidCanvas = gameView.getHolder.lockCanvas

          val newTime = java.lang.System.nanoTime
          //delta time, in ms (all time measures are in nano)
          val dt = ((newTime - lastTime) / (1000*1000)).toLong
          lastTime = newTime

          gameLoopStep(dt, Graphics.AndroidCanvas(androidCanvas))

          gameView.getHolder.unlockCanvasAndPost(androidCanvas)
        }

        val endTime: Long = java.lang.System.nanoTime
        val elapsedTime: Long = (endTime - beginTime)/(1000l*1000l)

        val sleepTime: Long = targetFramePeriod.map(fp => fp - elapsedTime).getOrElse(0)

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $elapsedTime.")
        }
      }
    }
  }

  class GameView(attributeSet: AttributeSet) extends SurfaceView(AndroidApp.this)
                                             with SurfaceHolder.Callback {
  
    private implicit val LogTag = Logger.Tag("sgl-gameview")
  
    getHolder.addCallback(this)
    this.setFocusable(true)
    this.setContentDescription("Main View where the game is rendered.")
  
    override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
      logger.debug("SurfaceChanged called")
    }
  
    override def surfaceCreated(holder: SurfaceHolder): Unit = {
      logger.debug("SurfaceCreated called")
      surfaceReady = true
    }
  
    override def surfaceDestroyed(holder: SurfaceHolder): Unit = {
      logger.debug("SurfaceDestroyed called")
      surfaceReady = false

      // We must ensure that the thread that is accessing this surface
      // does no longer run at the end of the surfaceDestroyed callback,
      // so we join on the thread.

      // this is safe to call twice (also called in onPause)
      gameLoop.running = false
      // The thread should finish the current frame and then return the run method,
      // so we just join on the thread to make sure there are no more rendering calls
      // after returning this callback.
      gameLoopThread.join
    }
  
  }

}
