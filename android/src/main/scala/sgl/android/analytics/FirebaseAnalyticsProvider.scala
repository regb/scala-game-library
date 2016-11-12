package sgl
package android
package analytics

import sgl.analytics._

import com.google.firebase.analytics._

import _root_.android.os.Bundle

trait FirebaseAnalyticsProvider extends AnalyticsProvider with Lifecycle {
  this: GameStateComponent with AndroidWindowProvider =>

  private var firebaseAnalytics: FirebaseAnalytics = null

  abstract override def startup(): Unit = {
    super.startup()
    firebaseAnalytics = FirebaseAnalytics.getInstance(mainActivity)
  }

  case class Event(name: String, params: EventParams) extends AbstractEvent
  object Event extends EventCompanion {
    override def createEvent(name: String, params: EventParams) = Event(name, params)

    override def levelUpEvent(level: Option[Long]): Event = 
      Event(FirebaseAnalytics.Event.LEVEL_UP, EventParams(level=level))
    override def shareEvent(itemId: Option[String]): Event =
      Event(FirebaseAnalytics.Event.SHARE, EventParams(itemId=itemId))
    override def gameOverEvent(score: Option[Long], map: Option[String]): Event =
      Event("game_over", EventParams(score=score,map=map))
    override def beginTutorialEvent(): Event =
      Event(FirebaseAnalytics.Event.TUTORIAL_BEGIN, EventParams())
    override def completeTutorialEvent(): Event =
      Event(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, EventParams())
    override def postScore(score: Long, level: Option[Long], character: Option[String]): Event =
      Event(FirebaseAnalytics.Event.POST_SCORE, EventParams(score=Some(score), level=level, character=character))
  }

  class Analytics extends AbstractAnalytics {

    private def paramsToBundle(params: EventParams): Bundle = {
      val bundle = new Bundle
      params.level.foreach(lvl => bundle.putLong(FirebaseAnalytics.Param.LEVEL, lvl))
      params.value.foreach(v => bundle.putDouble(FirebaseAnalytics.Param.VALUE, v))
      params.itemId.foreach(id => bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id))
      params.score.foreach(s => bundle.putLong(FirebaseAnalytics.Param.SCORE, s))
      params.map.foreach(m => bundle.putString("level_map", m))
      params.character.foreach(c => bundle.putString(FirebaseAnalytics.Param.CHARACTER, c))
      bundle
    }

    override def logEvent(event: Event): Unit = {
      firebaseAnalytics.logEvent(event.name, paramsToBundle(event.params))
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = ???
  }
  override val Analytics: Analytics = new Analytics

}
