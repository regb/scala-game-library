package sgl
package analytics

import util.LoggingProvider

/** An implementation of the Analytics module that only logs.
  *
  * This implementation relies on the logging module to log each event but it
  * does not send the data to an analysis service. One could technically
  * collect the logs to extract the data, but more likely this can be used when
  * one does not wish (or can't) to send analytics data.
  */
trait LoggedAnalyticsProvider extends AnalyticsProvider { this:
GameStateComponent with LoggingProvider =>

  class LoggedAnalytics extends Analytics {

    implicit val tag = Logger.Tag("analytics")

    // TODO: log more details

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      logger.info("Ignoring event to log: " + name)
    }

    override def logLevelUpEvent(level: Long): Unit = {
      logger.info("Ignoring event to log: Level Up")
    }
    override def logLevelStartEvent(level: String): Unit = {
      logger.info("Ignoring event to log: Level start")
    }
    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      logger.info("Ignoring event to log: Level end")
    }
    override def logShareEvent(itemId: Option[String]): Unit = {
      logger.info("Ignoring event to log: Share")
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      logger.info("Ignoring event to log: Game Over")
    }
    override def logBeginTutorialEvent(): Unit = {
      logger.info("Ignoring event to log: Begin Tutorial")
    }
    override def logCompleteTutorialEvent(): Unit = {
      logger.info("Ignoring event to log: Complete Tutorial")
    }
    override def logUnlockAchievementEvent(achievement: String): Unit = {
      logger.info("Ignoring event to log: unlock achievement")
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      logger.info("Ignoring event to log: Post Score")
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      logger.info("Ignoring new game screen to log: " + gameScreen)
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      logger.info("Ignoring new player property: " + name)
    }
  }

  override val Analytics: Analytics = new LoggedAnalytics

}
