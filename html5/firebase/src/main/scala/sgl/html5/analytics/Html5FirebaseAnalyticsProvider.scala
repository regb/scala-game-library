package sgl
package html5
package analytics

import sgl.analytics._

import scala.scalajs.js

/** Provides an implementaiton of the sgl.util.AnalyticsProvider interface with Firebase analytics. */
trait Html5FirebaseAnalyticsProvider extends AnalyticsProvider {
  self: GameStateComponent =>

  object FirebaseAnalytics extends Analytics {

    private def putAnalyticsValue(params: js.Dictionary[Any], key: String, value: AnalyticsValue): Unit = value match {
      case AnalyticsStringValue(v) => params(key) = v
      case AnalyticsLongValue(v) => params(key) = v.toDouble
      case AnalyticsDoubleValue(v) => params(key) = v
      case AnalyticsBooleanValue(v) => params(key) = if(v) 1 else 0
    }

    private def withCustoms(customs: Seq[(String, AnalyticsValue)])(params: js.Dictionary[Any] = js.Dictionary.empty[Any]): js.Dictionary[Any] = {
      customs.foreach { case (k, v) => putAnalyticsValue(params, k, v) }
      params
    }

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      val dict = js.Dictionary.empty[Any]
      params.level.foreach(lvl => dict("level") = lvl.toDouble)
      params.value.foreach(v => dict("value") = v)
      params.itemId.foreach(id => dict("item_id") = id)
      params.score.foreach(s => dict("score") = s)
      params.levelName.foreach(m => dict("level_name") = m)
      params.character.foreach(c => dict("character") = c)
      params.customs.foreach{ case (k, v) => putAnalyticsValue(dict, k, v) }
      js.Dynamic.global.firebase.analytics().logEvent(name, dict)
    }

    override def logLevelUpEvent(level: Long, customs: (String, AnalyticsValue)*): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_up", withCustoms(customs)(js.Dictionary("level" -> level.toDouble)))
    }

    override def logLevelEndEvent(levelName: String, success: Boolean, customs: (String, AnalyticsValue)*): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_end", withCustoms(customs)(js.Dictionary("level_name" -> levelName, "success" -> (if(success) 1 else 0))))
    }
    override def logLevelStartEvent(levelName: String, customs: (String, AnalyticsValue)*): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_start", withCustoms(customs)(js.Dictionary("level_name" -> levelName)))
    }

    override def logShareEvent(itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)()
      itemId.foreach(iid => params("item_id") = iid)
      js.Dynamic.global.firebase.analytics().logEvent("share", params)
    }
    override def logGameOverEvent(score: Option[Long], levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)()
      score.foreach(s => params("score") = s.toDouble)
      levelName.foreach(m => params("level_name") = m)
      js.Dynamic.global.firebase.analytics().logEvent("game_over", params)
    }
    override def logBeginTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)()
      if(tutorialId.nonEmpty) params("tutorial_id") = tutorialId
      levelName.foreach(params("level_name") = _)
      js.Dynamic.global.firebase.analytics().logEvent("tutorial_begin", params)
    }
    override def logCompleteTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)()
      if(tutorialId.nonEmpty) params("tutorial_id") = tutorialId
      levelName.foreach(params("level_name") = _)
      js.Dynamic.global.firebase.analytics().logEvent("tutorial_complete", params)
    }

    override def logUnlockAchievementEvent(achievementId: String, customs: (String, AnalyticsValue)*): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("unlock_achievement", withCustoms(customs)(js.Dictionary("achievement_id" -> achievementId)))
    }

    override def logPurchaseEvent(transactionId: Option[String], value: Double, currency: String, itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)(js.Dictionary[Any]("value" -> value, "currency" -> currency))
      transactionId.foreach(id => params("transaction_id") = id)
      itemId.foreach(id => params("item_id") = id)
      js.Dynamic.global.firebase.analytics().logEvent("purchase", params)
    }

    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      val params = withCustoms(customs)(js.Dictionary[Any]("score" -> score.toDouble))
      level.foreach(l => params("level") = l.toDouble)
      character.foreach(c => params("character") = c)
      js.Dynamic.global.firebase.analytics().logEvent("post_score", params)
    }

    override def setGameScreen(gameScreen: String): Unit = {
      js.Dynamic.global.firebase.analytics().setCurrentScreen(gameScreen)
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      js.Dynamic.global.firebase.analytics().setUserProperties(js.Dynamic.literal(name -> value))
    }
  }

  override val Analytics: Analytics = FirebaseAnalytics

}
