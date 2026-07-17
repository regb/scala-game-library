package sgl
package analytics

import sgl.util.LoggingProvider

trait LoggedAnalyticsProvider extends AnalyticsProvider {
  this: LoggingProvider =>

  override type Analytics = LoggedAnalytics

  override val Analytics = new LoggedAnalytics

  private implicit val LogTag: Logger.Tag = Logger.Tag("analytics")

  class LoggedAnalytics extends AbstractAnalytics {
    private def renderCustoms(customs: Seq[(String, AnalyticsValue)]): Map[String, Any] = customs.map {
      case (key, AnalyticsStringValue(value)) => key -> value
      case (key, AnalyticsLongValue(value)) => key -> value
      case (key, AnalyticsDoubleValue(value)) => key -> value
      case (key, AnalyticsBooleanValue(value)) => key -> value
    }.toMap

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      logger.info(s"custom_event: {name=${name}, params=${params}}")
    }

    override def logLevelUpEvent(level: Long, customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"level_up: {level=${level}, customs=${renderCustoms(customs)}}")
    }
    override def logLevelStartEvent(levelName: String, customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"level_start: {level_name=${levelName}, customs=${renderCustoms(customs)}}")
    }
    override def logLevelEndEvent(levelName: String, success: Boolean, customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"level_end: {level_name=${levelName}, success=${success}, customs=${renderCustoms(customs)}}")
    }
    override def logShareEvent(itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"share: {item_id=${itemId}, customs=${renderCustoms(customs)}}")
    }
    override def logGameOverEvent(score: Option[Long], levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"game_over: {score=${score}, level_name=${levelName}, customs=${renderCustoms(customs)}}")
    }
    override def logBeginTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"begin_tutorial: {tutorial_id=${tutorialId}, level_name=${levelName}, customs=${renderCustoms(customs)}}")
    }
    override def logCompleteTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"complete_tutorial: {tutorial_id=${tutorialId}, level_name=${levelName}, customs=${renderCustoms(customs)}}")
    }
    override def logUnlockAchievementEvent(achievement: String, customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"unlock_achievement: {achievement=${achievement}, customs=${renderCustoms(customs)}}")
    }
    override def logPurchaseEvent(transactionId: Option[String], value: Double, currency: String, itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"purchase: {transaction_id=${transactionId}, value=${value}, currency=${currency}, item_id=${itemId}, customs=${renderCustoms(customs)}}")
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String], customs: (String, AnalyticsValue)*): Unit = {
      logger.info(s"post_score: {score=${score}, level=${level}, character=${character}, customs=${renderCustoms(customs)}}")
    }

    override def setGameScreen(gameScreen: String): Unit = {
      logger.info(s"setting current game screen: $gameScreen")
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      logger.info(s"setting player property: $name=$value")
    }
  }
}
