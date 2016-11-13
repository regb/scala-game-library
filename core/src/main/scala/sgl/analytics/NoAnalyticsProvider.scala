package sgl
package analytics

import util.LoggingProvider

trait NoAnalyticsProvider extends AnalyticsProvider {
  this: GameStateComponent with LoggingProvider =>

  case class Event(name: String, params: EventParams) extends AbstractEvent

  object Event extends EventCompanion {
    override def createEvent(name: String, params: EventParams): Event = Event(name, params)
    
    override def levelUpEvent(level: Option[Long]): Event = Event("level_up", EventParams(level=level))
    override def shareEvent(itemId: Option[String]): Event = Event("share", EventParams(itemId=itemId))
    override def gameOverEvent(score: Option[Long], map: Option[String]): Event =
      Event("game_over", EventParams(score=score, map=map))
    override def beginTutorialEvent(): Event = Event("begin_tutorial", EventParams())
    override def completeTutorialEvent(): Event =
      Event("complete_tutorial", EventParams())
    override def postScore(score: Long, level: Option[Long], character: Option[String]): Event =
      Event("post_score", EventParams(score=Some(score), level=level, character=character))
  }


  class Analytics extends AbstractAnalytics {

    implicit val tag = Logger.Tag("NoAnalyticsProvider")

    override def logEvent(event: Event): Unit = {
      logger.info("Ignoring event to log: " + event)
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = {
      logger.info("Ignoring new game screen to log: " + gameScreen)
    }

  }

  override val Analytics: Analytics = new Analytics

}
