package sgl
package android
package analytics

import sgl.analytics._

import com.google.firebase.analytics.{FirebaseAnalytics => UFirebaseAnalytics, _}

import _root_.android.app.Activity
import _root_.android.os.Bundle

trait AndroidFirebaseAnalyticsProvider extends Activity with AnalyticsProvider {
  this: GameStateComponent =>

  private var firebaseAnalytics: UFirebaseAnalytics = null

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    firebaseAnalytics = UFirebaseAnalytics.getInstance(this)
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
      params.map.foreach(m => bundle.putString("level_map", m))
      params.character.foreach(c => bundle.putString(UFirebaseAnalytics.Param.CHARACTER, c))
      bundle
    }

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      firebaseAnalytics.logEvent(name, paramsToBundle(params))
    }

    override def logLevelUpEvent(level: Option[Long]): Unit = {
      val bundle = new Bundle
      level.foreach(lvl => bundle.putLong(UFirebaseAnalytics.Param.LEVEL, lvl))
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.LEVEL_UP, bundle)
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

    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      val bundle = new Bundle
      level.foreach(lvl => bundle.putLong(UFirebaseAnalytics.Param.LEVEL, lvl))
      bundle.putLong(UFirebaseAnalytics.Param.SCORE, score)
      character.foreach(c => bundle.putString(UFirebaseAnalytics.Param.CHARACTER, c))
      firebaseAnalytics.logEvent(UFirebaseAnalytics.Event.POST_SCORE, bundle)
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = {
      //TODO: maybe just log an event?
    }
  }

  override val Analytics: Analytics = FirebaseAnalytics

}
