package sgl.android

import sgl.util._

import sgl.{android => adr, _}

import android.app.Activity
import android.content.Intent
import android.os.Bundle

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
    //TODO: add a relative layout?
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
    this.startup()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    //lifecycle of SGL
    this.shutdown()
  }

  override def onResume(): Unit = {
    super.onResume()
    logger.trace("onResumed called")

    isRunning = true

    //TODO: need to check that onSurfaceCreated callback and onResume
    //      can never happen in two interlived thread (I'm assuming they
    //      come both from the main UI thread), else we might miss a resume event
    if(gameView.surfaceValid)
      this.resume()
  }

  override def onPause(): Unit = {
    super.onPause()
    logger.trace("onPause called")
    isRunning = false
    if(gameView.surfaceValid) //if surface not valid, it means the GameView already fired the pause event
      this.pause()
  }

}
