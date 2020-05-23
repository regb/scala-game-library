package sgl
package html5
package analytics

import sgl.analytics._

import scala.scalajs.js

/** Provides an implementaiton of the sgl.util.AnalyticsProvider interface with Firebase analytics.
  *
  * This maps all the standard analytics funciton to functions exported by the
  * firebase analytics for web. If you want to use this implementation, you will
  * need to add the firebase dependencies yourself when building a web application
  * and inheriting from this trait for the anlaytics provider. To set up firebase,
  * you should follow the official documentation from Firebase, but it should look
  * something like that in your html file:
  *
  *   <script src="https://www.gstatic.com/firebasejs/7.14.5/firebase-app.js"></script>
  *   <script src="https://www.gstatic.com/firebasejs/7.14.5/firebase-analytics.js"></script>
  *   <script>
  *     var firebaseConfig = {
  *       apiKey: "",
  *       authDomain: "",
  *       databaseURL: "",
  *       projectId: "",
  *       storageBucket: "",
  *       messagingSenderId: "",
  *       appId: "",
  *       measurementId: ""
  *     };
  *     firebase.initializeApp(firebaseConfig);
  *     firebase.analytics();
  *   </script>
  *
  * This code should preferably be included before your own script.
  */
trait Html5FirebaseAnalyticsProvider extends AnalyticsProvider {
  self: GameStateComponent =>

  object FirebaseAnalytics extends Analytics {

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      val dict = js.Dictionary.empty[Any]
      params.level.foreach(lvl => dict("level") = lvl.toDouble)
      params.value.foreach(v => dict("value") = v)
      params.itemId.foreach(id => dict("item_id") = id)
      params.score.foreach(s => dict("score") = s)
      params.levelName.foreach(m => dict("level_map") = m)
      params.character.foreach(c => dict("character") = c)
      params.customs.foreach{ case (k, v) => dict(k) = v }
      js.Dynamic.global.firebase.analytics().logEvent(name, dict)
    }

    override def logLevelUpEvent(level: Long): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_up", js.Dynamic.literal("level" -> level.toDouble))
    }

    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_end", js.Dynamic.literal("level_name" -> level, "success" -> (if(success) 1 else 0)))
    }
    override def logLevelStartEvent(level: String): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("level_start", js.Dynamic.literal("level_name" -> level))
    }

    override def logShareEvent(itemId: Option[String]): Unit = {
      itemId match {
        case None => js.Dynamic.global.firebase.analytics().logEvent("share" )
        case Some(iid) => js.Dynamic.global.firebase.analytics().logEvent("share", js.Dynamic.literal("item_id" -> iid))
      }
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      val params = js.Dictionary.empty[Any]
      score.foreach(s => params("score") = s.toDouble)
      map.foreach(m => params("level_map") = m)
      js.Dynamic.global.firebase.analytics().logEvent("game_over", params)
    }
    override def logBeginTutorialEvent(): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("begin_tutorial")
    }
    override def logCompleteTutorialEvent(): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("complete_tutorial")
    }

    override def logUnlockAchievementEvent(achievementId: String): Unit = {
      js.Dynamic.global.firebase.analytics().logEvent("unlock_achievement", js.Dynamic.literal("achievement_id" -> achievementId))
    }

    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      val params = js.Dictionary[Any]("score" -> score.toDouble)
      level.foreach(l => params("level") = l.toDouble)
      character.foreach(c => params("character") = c)
      js.Dynamic.global.firebase.analytics().logEvent("post_score", params)
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      js.Dynamic.global.firebase.analytics().setCurrentScreen(gameScreen.name)
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      js.Dynamic.global.firebase.analytics().setUserProperties(js.Dynamic.literal(name -> value))
    }
  }

  override val Analytics: Analytics = FirebaseAnalytics

}
