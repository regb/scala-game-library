package sgl
package android
package analytics

import sgl.analytics._

import _root_.android.app.Activity
import _root_.android.os.Bundle

import com.google.android.gms.analytics.{Tracker, GoogleAnalytics, HitBuilders}

trait AndroidGAAnalyticsProvider extends Activity with AnalyticsProvider {
  this: GameStateComponent =>

  val TrackerId: String

  private var tracker: Tracker = null
  private var googleAnalytics: GoogleAnalytics = null

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    googleAnalytics = GoogleAnalytics.getInstance(this)
    googleAnalytics.setLocalDispatchPeriod(1800)

    tracker = googleAnalytics.newTracker(TrackerId)
    tracker.enableExceptionReporting(true)
    tracker.enableAutoActivityTracking(false)
  }


  object GAAnalytics extends Analytics {

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Custom")
        .setAction(name)
        .build()
      )
    }

    override def logLevelUpEvent(level: Option[Long]): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Progression")
        .setAction("Level Up")
      val event = level.fold(builder)(lvl => builder.setLabel(lvl.toString))
                       .build()
      tracker.send(event)
    }
    override def logShareEvent(itemId: Option[String]): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Social")
        .setAction("Share")
      val event = itemId.fold(builder)(id => builder.setLabel(id))
                        .build()
      tracker.send(event)
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Progression")
        .setAction("Game Over")
      val label = map match {
        case Some(m) => Some("Map " + m + score.map(sco => s" (Score $sco)").getOrElse(""))
        case None => score.map(sco => "Score " + sco.toString)
      }
      val event = label.foldLeft(builder)((b, lbl) => b.setLabel(lbl))
                       .build()
      tracker.send(event)
    }
    override def logBeginTutorialEvent(): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Progression")
        .setAction("Begin Tutorial")
      tracker.send(builder.build())
    }
    override def logCompleteTutorialEvent(): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Progression")
        .setAction("Complete Tutorial")
      tracker.send(builder.build())
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      val builder = new HitBuilders.EventBuilder()
        .setCategory("Progression")
        .setAction("Post Score")
      val label = "Score " + score + level.map(lvl => s" (Level: $lvl)").getOrElse("")
      val event = builder.setLabel(label).build()
      tracker.send(event)
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = {
      //TODO: this might be persisted and used for following events logging
      //      in practice this should be fine as they should all relate to the
      //      latest game screen logged, but in weird cases (custom game screen logging)
      //      this would not hold and we need to make it more explicit in the interface
      tracker.setScreenName(gameScreen.name)
      tracker.send(new HitBuilders.ScreenViewBuilder().build())
    }

  }

  override val Analytics: Analytics = GAAnalytics

  //TODO: maybe provide some overridable functions to map custom events and params to Google Analytics
  //      hierarchy (category/action/label). Could also provide ways to override default mapping
  //      of build-in events. Could be useful for backward compatibility after deciding to go for a
  //      new naming scheme

  //  private def paramsToBundle(params: EventParams): Bundle = {
  //    val bundle = new Bundle
  //    params.level.foreach(lvl => bundle.putLong(FirebaseAnalytics.Param.LEVEL, lvl))
  //    params.value.foreach(v => bundle.putDouble(FirebaseAnalytics.Param.VALUE, v))
  //    params.itemId.foreach(id => bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id))
  //    params.score.foreach(s => bundle.putLong(FirebaseAnalytics.Param.SCORE, s))
  //    params.map.foreach(m => bundle.putString("level_map", m))
  //    params.character.foreach(c => bundle.putString(FirebaseAnalytics.Param.CHARACTER, c))
  //    bundle
  //  }

}
