package sgl
package html5
package analytics

import sgl.analytics._

import scala.scalajs.js

/** An AnalyticsProvider using Firebase through Cordova.
  *
  * You will need to add the following plugin:
  *     cordova plugin add cordova-plugin-firebase-analytics
  *
  * You will also need to add the google services config with the plugin:
  *     cordova plugin add cordova-support-google-services
  * and adding the files on config.xml that you get from firebase when
  * configuring your app:
  *
  *     <platform name="android">
  *       <resource-file src="google-services.json" target="app/google-services.json" />
  *       ...
  *     </platform>
  *     <platform name="ios">
  *       <resource-file src="GoogleService-Info.plist" />
  *       ...
  *     </platform>
  */
trait CordovaFirebaseAnalyticsProvider extends AnalyticsProvider {
  self: GameStateComponent =>

  object CordovaFirebaseAnalytics extends Analytics {

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      val dict = js.Dictionary.empty[Any]
      params.level.foreach(lvl => dict("level") = lvl.toDouble)
      params.value.foreach(v => dict("value") = v)
      params.itemId.foreach(id => dict("item_id") = id)
      params.score.foreach(s => dict("score") = s)
      params.levelName.foreach(m => dict("level_map") = m)
      params.character.foreach(c => dict("character") = c)
      params.customs.foreach{ case (k, v) => dict(k) = v }
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent(name, dict)
    }

    override def logLevelUpEvent(level: Long): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("level_up", js.Dynamic.literal("level" -> level.toDouble))
    }

    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("level_end", js.Dynamic.literal("level_name" -> level, "success" -> (if(success) 1 else 0)))
    }
    override def logLevelStartEvent(level: String): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("level_start", js.Dynamic.literal("level_name" -> level))
    }

    override def logShareEvent(itemId: Option[String]): Unit = {
      itemId match {
        case None => js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("share" )
        case Some(iid) => js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("share", js.Dynamic.literal("item_id" -> iid))
      }
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      val params = js.Dictionary.empty[Any]
      score.foreach(s => params("score") = s.toDouble)
      map.foreach(m => params("level_map") = m)
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("game_over", params)
    }
    override def logBeginTutorialEvent(): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("begin_tutorial")
    }
    override def logCompleteTutorialEvent(): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("complete_tutorial")
    }

    override def logUnlockAchievementEvent(achievementId: String): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("unlock_achievement", js.Dynamic.literal("achievement_id" -> achievementId))
    }

    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      val params = js.Dictionary[Any]("score" -> score.toDouble)
      level.foreach(l => params("level") = l.toDouble)
      character.foreach(c => params("character") = c)
      js.Dynamic.global.cordova.plugins.firebase.analytics.logEvent("post_score", params)
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.setCurrentScreen(gameScreen.name)
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      js.Dynamic.global.cordova.plugins.firebase.analytics.setUserProperty(name, value)
    }
  }

  override val Analytics: Analytics = CordovaFirebaseAnalytics

}
