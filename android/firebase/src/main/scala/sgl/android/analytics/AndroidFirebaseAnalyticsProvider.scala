package sgl
package android
package analytics

import sgl.analytics._

import com.google.firebase.analytics.{FirebaseAnalytics => UFirebaseAnalytics, _}

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.provider.Settings

/** An AnalyticsProvider using Firebase Analytics for Android.
  *
  * You will need to setup a Firebase project with Firebase and associate your
  * Android app with it, then at some point they will offer to download a
  * "google-services.json" file which contain a bunch of config (in JSON) for
  * your project and app. The file is meant for the standard Android Studio and
  * won't work with SGL, but you can extract the relevant information by hand
  * and configure it properly in your Android project.
  *
  * Firebase Analytics only requires one of these values, the App ID, which you
  * can find in the Firebase console on the project's settings for the app. It
  * looks like that:
  *
  *    1:1035469437089:android:73a4fb8297b2cd4f
  * 
  * The value is also in the google-services.json file, under the object
  * client.client_info.mobilesdk_app_id.  Either way, once you figure out your
  * own app id, you should provide it as an XML resource in the
  * res/values/strings.xml resource file in your android project, for example:
  *
  *    <string name="google_app_id" translatable="false">1:1035469437089:android:73a4fb8297b2cd4f</string>
  *
  * Firebase uses a bunch of other values, but it doesn't seem like they are
  * necessary to connect to Firebase Analytics, so you don't need to configure
  * them in your project.
  *
  * See also this blog post for more in depth discussion of manually
  * implementing the google-services.json:
  *   https://medium.com/@samstern_58566/how-to-use-firebase-on-android-without-the-google-services-plugin-93ecc7dc6c4
  *
  * Alternatively you may want to install an SBT plugin that understands this
  * file.
  */
trait AndroidFirebaseAnalyticsProvider extends Activity with AnalyticsProvider
{ self: GameStateComponent =>

  // TODO: Provide an override to set the google_app_id here and figure out
  //       how to dynamically instantiate the FirebaseApp. That would make
  //       the need for this value explicit (if the val is abstract) and remove
  //       the need for a strings.xml value.
  //       Or maybe not, FirebaseOptions could provide that but seems buggy?
  //         https://github.com/firebase/quickstart-android/issues/458
  //       In any case, the strings.xml is a fine way to initialize in most cases.

  private var firebaseAnalytics: UFirebaseAnalytics = null

  override def onCreate(bundle: Bundle): Unit = {
    firebaseAnalytics = UFirebaseAnalytics.getInstance(this)

    // We explicitly disable the analytics if we are running in the test lab of
    // firebase. This is actually used by prelaunch report on Google Play, so
    // this is going to be common across most Android apps, and we do not want
    // to collect noisy analytics from these prelaunch reports.
    val testLabSetting = Settings.System.getString(getContentResolver(), "firebase.test.lab")
    if(testLabSetting == "true") {
      // We are in a testlab environment, let's not collect any Analytics.
      // In practice I still see some events collected by testlab instances and
      // firebase seems to count these in the DAU. I think it might be because
      // the first_open event is still collected just before we disabled
      // analytics.
      firebaseAnalytics.setAnalyticsCollectionEnabled(false)
    }

    // We do the super.onCreate only after we potentially disabled collection, in case
    // that's when some auto-collected events are collected.
    super.onCreate(bundle)
  }

  //TODO: the name choice conflicts with firebase API (see import) so maybe
  //      we should sacrifice our name to avoid confusion? But in the end
  //      clients only see that class, so having this name as clean as possible
  //      make sense as well
  object FirebaseAnalytics extends Analytics {

    private def paramsToBundle(params: EventParams): Bundle = {
      val bundle = new Bundle
      params.level.foreach(lvl => bundle.putLong(UFirebaseAnalytics.Param.LEVEL, lvl))
      params.value.foreach(v => bundle.putDouble(UFirebaseAnalytics.Param.VALUE, v))
      params.itemId.foreach(id => bundle.putString(UFirebaseAnalytics.Param.ITEM_ID, id))
      params.score.foreach(s => bundle.putLong(UFirebaseAnalytics.Param.SCORE, s))
      params.levelName.foreach(m => bundle.putString("level_map", m))
      params.character.foreach(c => bundle.putString(UFirebaseAnalytics.Param.CHARACTER, c))
      params.customs.foreach{ case (k, v) => bundle.putString(k, v) }
      bundle
    }

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      firebaseAnalytics.logEvent(name, paramsToBundle(params))
    }

    override def logLevelUpEvent(level: Long): Unit = {
      val bundle = new Bundle
      bundle.putLong(UFirebaseAnalytics.Param.LEVEL, level)
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.LEVEL_UP, bundle)
    }

    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      val bundle = new Bundle
      bundle.putString(UFirebaseAnalytics.Param.LEVEL_NAME, level)
      bundle.putLong(UFirebaseAnalytics.Param.SUCCESS, if(success) 1 else 0)
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.LEVEL_END, bundle)
    }
    override def logLevelStartEvent(level: String): Unit = {
      val bundle = new Bundle
      bundle.putString(UFirebaseAnalytics.Param.LEVEL_NAME, level)
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.LEVEL_START, bundle)
    }

    override def logShareEvent(itemId: Option[String]): Unit = {
      val bundle = new Bundle
      itemId.foreach(id => bundle.putString(UFirebaseAnalytics.Param.ITEM_ID, id))
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.SHARE, bundle)
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      val bundle = new Bundle
      score.foreach(s => bundle.putLong(UFirebaseAnalytics.Param.SCORE, s))
      map.foreach(m => bundle.putString("level_map", m))
      firebaseAnalytics.logEvent("game_over", bundle)
    }
    override def logBeginTutorialEvent(): Unit = {
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.TUTORIAL_BEGIN, new Bundle)
    }
    override def logCompleteTutorialEvent(): Unit = {
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.TUTORIAL_COMPLETE, new Bundle)
    }

    override def logUnlockAchievementEvent(achievementId: String): Unit = {
      val bundle = new Bundle
      bundle.putString(UFirebaseAnalytics.Param.ACHIEVEMENT_ID, achievementId)
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle)
    }

    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      val bundle = new Bundle
      level.foreach(lvl => bundle.putLong(UFirebaseAnalytics.Param.LEVEL, lvl))
      bundle.putLong(UFirebaseAnalytics.Param.SCORE, score)
      character.foreach(c => bundle.putString(UFirebaseAnalytics.Param.CHARACTER, c))
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.POST_SCORE, bundle)
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      firebaseAnalytics.setCurrentScreen(self, gameScreen.name, null)
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      firebaseAnalytics.setUserProperty(name, value)
    }
  }

  override val Analytics: Analytics = FirebaseAnalytics

}
