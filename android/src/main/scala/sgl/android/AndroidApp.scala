package sgl.android

import sgl.util._

import sgl.{android => adr, _}

import android.app.Activity
import android.content.Intent
import android.os.Bundle

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
                    with AndroidWindowProvider with ThreadBasedGameLoopProvider
                    with AndroidSystemProvider with GameStateComponent {

  this: LoggingProvider =>

  /** Indicates if the activity is in running state
    *
    * This checks that the activity is between calls
    * onResume and onPause. We use this in the SurfaceHolder.Callback
    * to check if we should fire up a resume event for the app.
    */
  var isRunning: Boolean = false

  private implicit val LogTag: Logger.Tag = Logger.Tag("sgl-main-activity")

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    logger.trace("onCreated called")

    gameView = new GameView(this)
    setContentView(gameView)

    /* make sure that if the activity is launched again and the previous
     * one is still around, it will finish the new one and use the existing one
     */
    val intent = getIntent()
    if(!isTaskRoot() && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && 
                        Intent.ACTION_MAIN.equals(intent.getAction())) {
      logger.warning("Main Activity is not the root.  Finishing Main Activity instead of launching.")
      finish()
    }

    this.registerInputsListeners()

    //lifecycle of SGL
    lifecycleListener.startup()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    //lifecycle of SGL
    lifecycleListener.shutdown()
  }

  override def onResume(): Unit = {
    super.onResume()
    logger.trace("onResumed called")

    isRunning = true

    //TODO: need to check that onSurfaceCreated callback and onResume
    //      can never happen in two interlived thread (I'm assuming they
    //      come both from the main UI thread), else we might miss a resume event
    if(gameView.surfaceValid)
      lifecycleListener.resume()
  }

  override def onPause(): Unit = {
    super.onPause()
    logger.trace("onPause called")
    isRunning = false
    if(gameView.surfaceValid) //if surface not valid, it means the GameView already fired the pause event
      lifecycleListener.pause()
  }

  class GameLoop extends Runnable {

    private implicit val Tag = Logger.Tag("game-loop")

    private val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true

    override def run(): Unit = {
      var lastTime: Long = java.lang.System.nanoTime

      while(running) {
        val beginTime: Long = java.lang.System.nanoTime

        if(gameView.surfaceValid) {
          val androidCanvas = gameView.getHolder.lockCanvas

          val newTime = java.lang.System.nanoTime
          //delta time, in ms (all time measures are in nano)
          val dt = ((newTime - lastTime) / (1000*1000)).toLong
          lastTime = newTime

          gameLoopStep(dt, Graphics.AndroidCanvas(androidCanvas))

          if(gameView.surfaceValid)
            gameView.getHolder.unlockCanvasAndPost(androidCanvas)
        }

        val currentTime: Long = java.lang.System.nanoTime
        val timeForScheduler: Long = targetFramePeriod.map(fp => fp - (currentTime - beginTime)/(1000l*1000l)).getOrElse(10l)
        Scheduler.run(timeForScheduler)

        val endTime: Long = java.lang.System.nanoTime
        val elapsedTime: Long = endTime - beginTime

        val sleepTime: Long = targetFramePeriod.map(fp => fp - elapsedTime/(1000l*1000l)).getOrElse(0)

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $elapsedTime.")
        }
      }

      Scheduler.shutdown()
    }
  }

  class GameView(attributeSet: AttributeSet extends SurfaceView(AndroidApp.this)
                                            with SurfaceHolder.Callback {
  
    private implicit val LogTag = gameActivity.Logger.Tag("sgl-gameview")
  
    getHolder.addCallback(this)
    setFocusable(true)
  
    var surfaceValid: Boolean = false
  
    override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
      gameActivity.logger.debug("SurfaceChanged called")
    }
  
    override def surfaceCreated(holder: SurfaceHolder): Unit = {
      gameActivity.logger.debug("SurfaceCreated called")
      surfaceValid = true
      if(gameActivity.isRunning)
        gameActivity.resume()
    }
  
    override def surfaceDestroyed(holder: SurfaceHolder): Unit = {
      gameActivity.logger.debug("SurfaceDestroyed called")
      surfaceValid = false
      if(gameActivity.isRunning) {
        gameActivity.pause()
        gameActivity.gameLoop.runningThread.join
      }
    }
  
    def this(gameActivity: AndroidApp) = this(gameActivity, null)
  
  }

}
