package sgl
package analytics

trait AnalyticsProvider {
  this: GameScreensComponent =>


  case class EventParams(
    value: Option[Double] = None, level: Option[Long] = None,
    itemId: Option[String] = None, score: Option[Long] = None,
    map: Option[String] = None
  )
  abstract class AbstractEvent {
    val name: String
    val params: EventParams
  }
  type Event <: AbstractEvent

  val Event: EventCompanion
  abstract class EventCompanion {
    def createEvent(name: String, params: EventParams): Event
    
    def levelUpEvent(level: Option[Long]): Event
    def shareEvent(itemId: Option[String]): Event
    def gameOverEvent(score: Option[Long], map: Option[String]): Event
  }


  abstract class AbstractAnalytics {

    def logEvent(event: Event): Unit

    def logGameScreen(gameScreen: GameScreen): Unit

  }
  type Analytics <: AbstractAnalytics

  val Analytics: Analytics

}
