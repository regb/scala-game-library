package sgl.android
package services

import sgl.util._

import android.app.Activity
import android.os.Bundle
import android.content.Intent

import com.google.android.gms.common.api.GoogleApiClient
import GoogleApiClient.{ConnectionCallbacks, OnConnectionFailedListener}
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.games.Games
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.games.GamesStatusCodes
import com.google.android.gms.drive.Drive

/** Provide google games services for the main activity
  * 
  * The role of this trait is to manage the lifecycle of the google api.
  * It correctly integrates with the DefaultGameActivity to properly
  * connect to the Google APIs and provide helper functions to perform
  * the standard operations.
  *
  * To use it, you simply extends it when declaring your main activity.
  *
  * It can be configured by overriding a few flags to control the exact
  * features you need for your game. It also provides an auto-login
  * management implementation.
  */
trait GoogleGamesServices extends Activity
                          with ConnectionCallbacks with OnConnectionFailedListener {

  this: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl-google-games-services")

  private val sglConfigSave: AndroidSave = new AndroidSave("sgl-google-games-services-config", this)
  private def setAutoLogin(b: Boolean): Unit = sglConfigSave.putBoolean("google-play-auto-login", b)
  private def autoLogin: Boolean = sglConfigSave.getBooleanOrElse("google-play-auto-login", true)

  /** override to set auto login on startup
    *
    * This only tries to auto login on the first startup of the app.
    * On successive startup, it will perform the auto-login if the
    * user had accepted to login the last time. If the player had
    * refused the login, then this will not perform auto-login
    *
    * this is to avoid frustating experience with players that don't
    * want to use google play services, but are still asked to login 
    * on each startup of the app.
    */
  val GoogleGamesAutoLogin = true
  /** override to set support for saved games 
    *
    * Saved games require to activate the Google Drive API in your
    * Google developer console configuration for your game.
    */
  val GoogleGamesSavedGames = false

  var googleApiClient: GoogleApiClient = null


  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    val builderBase = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(Games.API).addScope(Games.SCOPE_GAMES)
    val builderFinal = if(GoogleGamesSavedGames)
                         builderBase.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                       else
                         builderBase

    googleApiClient = builderFinal.build()
  }

  override def onStart(): Unit = {
    super.onStart()
    if(googleApiClient != null && autoLogin) {
      logger.trace("Connecting to google api")
      googleApiClient.connect()
    }
  }

  override def onStop(): Unit = {
    super.onStop()
    if(googleApiClient != null)
      googleApiClient.disconnect()
  }


  override def onConnected(bundle: Bundle): Unit = {
    logger.trace("onConnected from google play")
    setAutoLogin(true)
  }

  override def onConnectionSuspended(cause: Int): Unit = {
    logger.trace("onConnectedSuspend from google play")
    if(googleApiClient != null && autoLogin) {
      googleApiClient.connect()
    }
  }


  // Request code to use when launching the resolution activity
  private val RequestResolveError = 1001
  // Bool to track whether the app is already resolving an error
  private var resolvingError = false;

  override def onConnectionFailed(result: ConnectionResult): Unit = {
    logger.trace("onConnectionFailed from google play: " + result)
    logger.trace("has resolution: " + result.hasResolution())
    if(resolvingError) {
      logger.trace("Ignoring connection failed because already resolving")
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
    logger.trace("onActivityResult called")
    if(requestCode == RequestResolveError) {
      resolvingError = false
      if(resultCode == Activity.RESULT_OK) {
        logger.trace("RESULT_OK, trying to connect again")
        googleApiClient.connect()
      } else {
        //just give up
        setAutoLogin(false)
      }
    }
  }

  def startDefaultLeaderboardActivity(leaderboardId: String): Unit = {
    runOnUiThread(new Runnable {
      override def run(): Unit = {
        if(googleApiClient != null && googleApiClient.isConnected()) {
          startActivityForResult(Games.Leaderboards.getLeaderboardIntent(googleApiClient, leaderboardId), 0)
        } else if(googleApiClient != null) {
          googleApiClient.connect()
        }
      }
    })
  }

  def startDefaultAchievementsActivity(): Unit = {
    runOnUiThread(new Runnable {
      override def run(): Unit = {
        if(googleApiClient != null && googleApiClient.isConnected()) {
          startActivityForResult(Games.Achievements.getAchievementsIntent(googleApiClient), 0)
        } else if(googleApiClient != null) {
          googleApiClient.connect()
        }
      }
    })
  }

  /*
   * It looks like wrapping unlock and increment really does not help much and using the direct
   * native interface of GooglaeAPI might be better. But I believe that hiding the lifecycle and
   * connection of the google api behind simpler functions is probably worth it, and should be
   * the main goal of the GoogleGamesServices trait layer.
   */

  //TODO: in this file we provide simple helpers without abstraction on top of google games services
  //      but it would be good to provide separately an Achievement abstraction layer with a fallback mecanisms
  //      to the local drive if there is no google games services available.

  def unlockAchievement(achievementId: String): Unit = {
    if(googleApiClient != null && googleApiClient.isConnected())
      Games.Achievements.unlock(googleApiClient, achievementId)
  }

  def incrementAchievement(achievementId: String, inc: Int): Unit = {
    if(googleApiClient != null && googleApiClient.isConnected())
      Games.Achievements.increment(googleApiClient, achievementId, inc)
  }

  def submitScoreLeaderboard(leaderboardId: String, score: Long): Unit = {
    if(googleApiClient != null && googleApiClient.isConnected())
      Games.Leaderboards.submitScore(googleApiClient, leaderboardId, score)
  }


}
