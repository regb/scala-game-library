package sgl
package analytics

/** An implementation of the Analytics module that does nothing.
  *
  * Use this if you want to totally ignore logging, for example
  * for a release version on a platform that has no good analytics
  * framework. Generally you should use LoggedAnalyticsProvider as
  * it helps with debugging, but this version is more lean with
  * less dependencies and it might make sense if you don't want
  * a release version to actually log the analytics.
  */
trait NoAnalyticsProvider extends AnalyticsProvider {
  this: GameStateComponent =>

  class NoAnalytics extends Analytics {

    override def logCustomEvent(name: String, params: EventParams): Unit = {}

    override def logLevelUpEvent(level: Long): Unit = {}
    override def logLevelStartEvent(level: String): Unit = {}
    override def logLevelEndEvent(level: String, success: Boolean): Unit = {}
    override def logShareEvent(itemId: Option[String]): Unit = {}
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {}
    override def logBeginTutorialEvent(): Unit = {}
    override def logCompleteTutorialEvent(): Unit = {}
    override def logUnlockAchievementEvent(achievement: String): Unit = {}
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {}

    override def setGameScreen(gameScreen: GameScreen): Unit = {}

    override def setPlayerProperty(name: String, value: String): Unit = {}
  }

  override val Analytics: Analytics = new NoAnalytics

}
