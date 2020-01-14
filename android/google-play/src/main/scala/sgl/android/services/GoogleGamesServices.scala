package sgl.android
package services

import sgl.util._

import android.app.Activity
import android.os.Bundle
import android.content.Intent

import com.google.android.gms.games.Games
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.{GoogleSignIn, GoogleSignInAccount, GoogleSignInClient, GoogleSignInOptions}
import com.google.android.gms.tasks.{OnCompleteListener, OnSuccessListener, Task}


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
trait GoogleGamesServices extends Activity {

  this: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl-google-games-services")

  // Status code for activity result
  private val RcSignIn = 7001
  private val RcAchievements = 8001
  private val RcLeaderboads = 9001

  private val SignInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
  // Client used to sign in with Google APIs
  private var googleSignInClient: GoogleSignInClient = null
  // The currently signed in account, used to check the account has changed outside of this activity when resuming.
  var signedInAccount: GoogleSignInAccount = null

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


  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    // Create the client used to sign in.
    googleSignInClient = GoogleSignIn.getClient(this, SignInOptions)
  }

  override def onResume(): Unit = {
    super.onResume()

    val account = GoogleSignIn.getLastSignedInAccount(this)
    if(account != null && GoogleSignIn.hasPermissions(account, SignInOptions.getScopeArray():_*)) {
      logger.debug("getLastSignedInAccount(): success")
      onConnected(account)
    } else {
      signInSilently()
    }
  }

  def signInSilently(): Unit = {
    googleSignInClient.silentSignIn().addOnCompleteListener(this,
      new OnCompleteListener[GoogleSignInAccount] {
        override def onComplete(task: Task[GoogleSignInAccount]): Unit = {
          if(task.isSuccessful()) {
            logger.debug("signInSilently(): success")
            onConnected(task.getResult())
          } else {
            logger.debug("signInSilently(): failure: " + task.getException())
            onDisconnected()
            if(autoLogin) {
              signInExplicitly()
            }
          }
        }
      })
  }

  def signInExplicitly(): Unit = {
    startActivityForResult(googleSignInClient.getSignInIntent(), RcSignIn)
  }


  private def onConnected(account: GoogleSignInAccount): Unit = {
    setAutoLogin(true)
    signedInAccount = account
  }

  private def onDisconnected(): Unit = {
    signedInAccount = null
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == RcSignIn) {
      val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
      if(result.isSuccess()) {
        signedInAccount = result.getSignInAccount()
      } else {
        // Just give up.
        setAutoLogin(false)
      }
    }
  }

  def startDefaultLeaderboardActivity(leaderboardId: String): Unit = {
    val that = this
    runOnUiThread(new Runnable {
      override def run(): Unit = {
        if(signedInAccount != null) {
          Games.getLeaderboardsClient(that, signedInAccount)
               .getLeaderboardIntent(leaderboardId)
               .addOnSuccessListener(new OnSuccessListener[Intent] {
                 override def onSuccess(intent: Intent): Unit = {
                   startActivityForResult(intent, RcLeaderboads)
                 }
               })
        } else {
          signInExplicitly()
        }
      }
    })
  }

  def startDefaultAchievementsActivity(): Unit = {
    val that = this
    runOnUiThread(new Runnable {
      override def run(): Unit = {
        if(signedInAccount != null) {
          Games.getAchievementsClient(that, signedInAccount)
               .getAchievementsIntent()
               .addOnSuccessListener(new OnSuccessListener[Intent] {
                 override def onSuccess(intent: Intent): Unit = {
                   startActivityForResult(intent, RcAchievements)
                 }
               })
        } else {
          signInExplicitly()
        }
      }
    })
  }

  //TODO: in this file we provide simple helpers without abstraction on top of google games services
  //      but it would be good to provide separately an Achievement abstraction layer with a fallback mecanisms
  //      to the local drive if there is no google games services available.

  def unlockAchievement(achievementId: String): Unit = {
    if(signedInAccount != null) {
      Games.getAchievementsClient(this, signedInAccount).unlock(achievementId)
    }
  }

  def incrementAchievement(achievementId: String, inc: Int): Unit = {
    if(signedInAccount != null) {
      Games.getAchievementsClient(this, signedInAccount).increment(achievementId, inc)
    }
  }

  def submitScoreLeaderboard(leaderboardId: String, score: Long): Unit = {
    if(signedInAccount != null) {
      Games.getLeaderboardsClient(this, signedInAccount).submitScore(leaderboardId, score)
    }
  }

}
