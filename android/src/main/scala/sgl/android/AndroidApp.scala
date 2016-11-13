package sgl.android

import sgl.util._

import sgl.{android => adr, _}

import android.app.Activity
import android.util.Log
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

  private val LogTag: String = "SGL-GameActivity"

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    Log.v(LogTag, "onCreated called")

    gameView = new GameView(this)
    //TODO: add a relative layout?
    setContentView(gameView)

    /* make sure that if the activity is launched again and the previous
     * one is still around, it will finish the new one and use the existing one
     */
    val intent = getIntent()
    if(!isTaskRoot() && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && 
                        Intent.ACTION_MAIN.equals(intent.getAction())) {
      Log.w(LogTag, "Main Activity is not the root.  Finishing Main Activity instead of launching.")
      finish()
    }

    //lifecycle of SGL
    this.startup()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    //lifecycle of SGL
    this.shutdown()
  }

  /* don't need to do anything on this lifecycle */
  //override def onStart(): Unit = {
  //  super.onStart()
  //  Log.v(LogTag, "onStart called")
  //}
  //override def onStop(): Unit = {
  //  super.onStop()
  //}

  override def onResume(): Unit = {
    super.onResume()
    Log.v(LogTag, "onResumed called")

    isRunning = true
    //TODO: need to check that onSurfaceCreated callback and onResume
    //      can never happen in two interlived thread (I'm assuming they
    //      come both from the main UI thread), else we might miss a resume event
    if(gameView.surfaceValid)
      this.resume()
  }

  override def onPause(): Unit = {
    super.onPause()
    Log.v(LogTag, "onPause called")
    isRunning = false
    if(gameView.surfaceValid) //if surface not valid, it means the GameView already fired the pause event
      this.pause()
  }

}
