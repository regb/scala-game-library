package sgl
package analytics

import util.LoggingProvider

trait NoAnalyticsProvider extends AnalyticsProvider {
  this: GameStateComponent with LoggingProvider =>


  class NoAnalytics extends Analytics {

    implicit val tag = Logger.Tag("NoAnalyticsProvider")

    override def logCustomEvent(name: String, params: EventParams): Unit = {
      logger.info("Ignoring event to log: " + name)
    }

    override def logLevelUpEvent(level: Option[Long]): Unit = {
      logger.info("Ignoring event to log: Level Up")
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
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      logger.info("Ignoring event to log: Post Score")
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = {
      logger.info("Ignoring new game screen to log: " + gameScreen)
    }
  }

  override val Analytics: Analytics = new NoAnalytics

}
