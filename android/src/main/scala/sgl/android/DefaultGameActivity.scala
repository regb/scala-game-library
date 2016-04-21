package sgl.android

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread

import com.google.android.gms.common.api.GoogleApiClient
import GoogleApiClient.{ConnectionCallbacks, OnConnectionFailedListener}
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.games.Games
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.games.GamesStatusCodes
import com.google.android.gms.drive.Drive

class DefaultGameActivity extends Activity
                          with ConnectionCallbacks with OnConnectionFailedListener {


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

  //TODO: we could probably move google games services API into a trait
  var googleApiClient: GoogleApiClient = null

  private val sglConfigSave: AndroidSave = new AndroidSave("sgl-config", this)
  def setAutoLogin(b: Boolean): Unit = sglConfigSave.putBoolean("google-play-auto-login", b)
  def autoLogin: Boolean = sglConfigSave.getBooleanOrElse("google-play-auto-login", true)

  //TODO: provide a config for auto-login on/off by default

  override def onCreate(bundle: Bundle) {
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
    } else {
      // Create the Google Api Client with access to the Play Games services
      googleApiClient = new GoogleApiClient.Builder(this)
                              .addConnectionCallbacks(this)
                              .addOnConnectionFailedListener(this)
                              .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                              .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                              .build()

    }
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    gameApp.shutdown()
  }

  override def onStart(): Unit = {
    super.onStart()
    Log.v(LogTag, "onStart called")

    if(googleApiClient != null && autoLogin) {
      Log.v(LogTag, "Connecting to google api")
      googleApiClient.connect()
    }
  }

  override def onStop(): Unit = {
    super.onStop()

    if(googleApiClient != null)
      googleApiClient.disconnect()
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

  override def onConnected(bundle: Bundle): Unit = {
    Log.v(LogTag, "onConnected from google play")
    setAutoLogin(true)
  }

  override def onConnectionSuspended(cause: Int): Unit = {
    Log.v(LogTag, "onConnectedSuspend from google play")
    if(googleApiClient != null && autoLogin) {
      googleApiClient.connect()
    }
  }


  // Request code to use when launching the resolution activity
  private val RequestResolveError = 1001
  // Bool to track whether the app is already resolving an error
  private var resolvingError = false;

  override def onConnectionFailed(result: ConnectionResult): Unit = {
    Log.v(LogTag, "onConnectionFailed from google play: " + result)
    Log.v(LogTag, "has resolution: " + result.hasResolution())
    if(resolvingError) {
      Log.v(LogTag, "Ignoring connection failed because already resolving")
    } else if(result.hasResolution()) {
      try {
        resolvingError = true
        result.startResolutionForResult(this, RequestResolveError)
      } catch {
        case (e: Exception) =>
          //give up
          resolvingError = false
          setAutoLogin(false)
      }
    } else {
      //just give up
      setAutoLogin(false)
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent): Unit = {
    Log.v(LogTag, "onActivityResult called")
    if(requestCode == RequestResolveError) {
      resolvingError = false
      if(resultCode == Activity.RESULT_OK) {
        Log.v(LogTag, "RESULT_OK, trying to connect again")
        googleApiClient.connect()
      } else {
        //just give up
        setAutoLogin(false)
      }
    }
  }

}
