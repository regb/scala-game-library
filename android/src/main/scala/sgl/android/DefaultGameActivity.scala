package sgl.android

import android.app.Activity
import android.util.Log
import android.content.Intent
import android.os.Bundle

class DefaultGameActivity extends Activity {

  /*
   * Not very happy with having to use vars, but this seems like
   * the way things work on Android, with a lot of stuff having
   * to be initialized in onCreate.
   */

  /** The reference to the AndroidApp cake
    *
    * This must be set in the onCreate method of the main activity
    * that will run. Simply invoke
    * {{{ gameApp = new AndroidApp { ... } }}}
    * before calling the super.onCreate
    */
  var gameApp: AndroidApp = null

  var gameView: GameView = null

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
    gameApp.startup()

    /* make sure that if the activity is launched again and the previous
     * one is still around, it will finish the new one and use the existing one
     */
    val intent = getIntent()
    if(!isTaskRoot() && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && 
                        Intent.ACTION_MAIN.equals(intent.getAction())) {
      Log.w(LogTag, "Main Activity is not the root.  Finishing Main Activity instead of launching.")
      finish()
    }
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    gameApp.shutdown()
  }

  override def onStart(): Unit = {
    super.onStart()
    Log.v(LogTag, "onStart called")
  }

  override def onStop(): Unit = {
    super.onStop()
  }

  override def onResume(): Unit = {
    super.onResume()
    Log.v(LogTag, "onResumed called")

    isRunning = true
    //TODO: need to check that onSurfaceCreated callback and onResume
    //      can never happen in two interlived thread (I'm assuming they
    //      come both from the main UI thread), else we might miss a resume event
    if(gameView.surfaceValid)
      gameApp.resume()
  }

  override def onPause(): Unit = {
    super.onPause()
    Log.v(LogTag, "onPause called")
    isRunning = false
    if(gameView.surfaceValid) //if surface not valid, it means the GameView already fired the pause event
      gameApp.pause()
  }
}
