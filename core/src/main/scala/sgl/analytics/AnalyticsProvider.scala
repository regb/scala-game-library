package sgl
package analytics

trait AnalyticsProvider {
  this: GameScreensComponent =>


  /** Optional parameters to an Event
    * 
    * itemId is a generic ID type that can identify the main
    * element in the Event.
    *
    * character is the identification of a character in the game
    *
    * level is not a map identifier, but the level of the player
    */
  case class EventParams(
    value: Option[Double] = None, level: Option[Long] = None,
    itemId: Option[String] = None, score: Option[Long] = None,
    map: Option[String] = None, character: Option[String] = None
  )
  abstract class AbstractEvent {
    val name: String
    val params: EventParams
  }
  type Event <: AbstractEvent

  val Event: EventCompanion
  abstract class EventCompanion {
    def createEvent(name: String, params: EventParams): Event
    
    /* Some predefined events, the implementation will either
     * map them to a semantically correspond events in the
     * analytics, or with a reasonable custom event
     * Note that we don't provide default implementation with standard
     * name, as the naming convention will be dependendent of the platform
     * used for analytics. This is also why you should try to use a
     * built-in event rather than your own custom event.
     */
    def levelUpEvent(level: Option[Long]): Event
    def shareEvent(itemId: Option[String]): Event
    def gameOverEvent(score: Option[Long], map: Option[String]): Event
    def beginTutorialEvent(): Event
    def completeTutorialEvent(): Event
    /** post a score in the game
      *
      * post might be a bit misleading, this has nothing to do with
      * sharing or posting on social media, it is just about completing
      * a score (scoring a score) in the game.
      */
    def postScore(score: Long, level: Option[Long], character: Option[String]): Event
  }


  abstract class AbstractAnalytics {

    def logEvent(event: Event): Unit

    def logGameScreen(gameScreen: GameScreen): Unit

  }
  type Analytics <: AbstractAnalytics

  val Analytics: Analytics

}
