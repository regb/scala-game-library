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
trait LoggedAnalyticsProvider extends AnalyticsProvider {
  this: GameStateComponent with LoggingProvider =>

  class LoggedAnalytics extends Analytics {

    implicit val tag = Logger.Tag("analytics")

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      logger.info(s"${name}: ${params}")
    }

    override def logLevelUpEvent(level: Long): Unit = {
      logger.info(s"level_up: {level=${level}}")
    }
    override def logLevelStartEvent(level: String): Unit = {
      logger.info(s"level_start: {level=${level}}")
    }
    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      logger.info(s"level_end: {level=${level}, success=${success}}")
    }
    override def logShareEvent(itemId: Option[String]): Unit = {
      logger.info(s"share: {item_id=${itemId}}")
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      logger.info(s"game_over: {score=${score}, map=${map}}")
    }
    override def logBeginTutorialEvent(): Unit = {
      logger.info(s"begin_tutorial")
    }
    override def logCompleteTutorialEvent(): Unit = {
      logger.info(s"complete_tutorial")
    }
    override def logUnlockAchievementEvent(achievement: String): Unit = {
      logger.info(s"unlock_achievement: {achievement=${achievement}}")
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      logger.info(s"post_score: {score=${score}, level=${level}, character=${character}}")
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      logger.info(s"setting current game screen: $gameScreen")
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      logger.info(s"setting player property ${name}=${value}")
    }
  }

  override val Analytics: Analytics = new LoggedAnalytics

}
