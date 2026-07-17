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

  override val Analytics: Analytics = new NoAnalytics

  override type Analytics = NoAnalytics

}  

class NoAnalytics extends AbstractAnalytics {

  override def logCustomEvent(name: String, params: EventParams): Unit = {}

  override def logLevelUpEvent(level: Long, customs: (String, AnalyticsValue)*): Unit = {}
  override def logLevelStartEvent(levelName: String, customs: (String, AnalyticsValue)*): Unit = {}
  override def logLevelEndEvent(levelName: String, success: Boolean, customs: (String, AnalyticsValue)*): Unit = {}
  override def logShareEvent(itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {}
  override def logGameOverEvent(score: Option[Long], levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {}
  override def logBeginTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {}
  override def logCompleteTutorialEvent(tutorialId: String, levelName: Option[String], customs: (String, AnalyticsValue)*): Unit = {}
  override def logUnlockAchievementEvent(achievement: String, customs: (String, AnalyticsValue)*): Unit = {}
  override def logPurchaseEvent(transactionId: Option[String], value: Double, currency: String, itemId: Option[String], customs: (String, AnalyticsValue)*): Unit = {}
  override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String], customs: (String, AnalyticsValue)*): Unit = {}

  override def setGameScreen(gameScreen: String): Unit = {}

  override def setPlayerProperty(name: String, value: String): Unit = {}
}
