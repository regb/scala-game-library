package sgl
package analytics

/*
 * It seems that we need to settle on a set of built-in, common, events
 * useful for games, and let the analytics backend interpret them and
 * convert them as closely as possible to its analytics framework.
 *
 * The type of events cannot be provided by the underlying providers, as the
 * client really needs to know what constructor/factory name to invoke,
 * without knowledge of the underlying analytics framework.
 *
 * We choose to go for two types of analytics, events and screen navigation.
 * An event is some specific action happening in the game, usually triggered
 * by a user, and can have parameters associated. Example would be "Killed an enemy",
 * or "Completed level 23" (with 23 as the parameter here). Navigation over
 * screens matches well with the SGL GameStateComponent framework, with everything
 * being based on GameScreens.
 *
 * We choose to not introduce an Event type, but rather have a list of methods
 * with names corresponding to type of events in the analytics class.
 * It did not seem like introducing an Event type, and have a logEvent(event: Event)
 * method on the analytics would be very helpful, as there is no other actions that
 * can be performed with Events. Essentially, clients would literally create an
 * event to send it right away to the analytics. Now, there be use-cases I did
 * not have in mind, for example having an Event type could let some implementations
 * provide some hooks to customize how certain events get translated to the underlying
 * system.
 * Another reason to go for the method list, without introducing an intermediate
 * Event object, is to avoid generating garbage for the garbage collector.
 */
trait AnalyticsProvider {
  this: GameStateComponent =>

  //abstract class AbstractEvent {
  //  val name: String
  //  val params: EventParams
  //}
  //type Event <: AbstractEvent

  //val Event: EventCompanion
  //abstract class EventCompanion {
  //  def createEvent(name: String, params: EventParams): Event
  //  
  //  /* Some predefined events, the implementation will either
  //   * map them to a semantically correspond events in the
  //   * analytics, or with a reasonable custom event
  //   * Note that we don't provide default implementation with standard
  //   * name, as the naming convention will be dependendent of the platform
  //   * used for analytics. This is also why you should try to use a
  //   * built-in event rather than your own custom event.
  //   */
  //  def levelUpEvent(level: Option[Long]): Event
  //  def shareEvent(itemId: Option[String]): Event
  //  def gameOverEvent(score: Option[Long], map: Option[String]): Event
  //  def beginTutorialEvent(): Event
  //  def completeTutorialEvent(): Event
  //  /** post a score in the game
  //    *
  //    * post might be a bit misleading, this has nothing to do with
  //    * sharing or posting on social media, it is just about completing
  //    * a score (scoring a score) in the game.
  //    */
  //  def postScore(score: Long, level: Option[Long], character: Option[String]): Event
  //}


  abstract class Analytics {

    /** Log a generic event */
    def logCustomEvent(name: String, params: EventParams): Unit

    /* log semantics events 
     * Some predefined events, the implementation will either
     * map them to a semantically correspond events in the
     * analytics, or with a reasonable custom event
     * Note that we don't provide default implementation with standard
     * name, as the naming convention will be dependendent of the platform
     * used for analytics. This is also why you should try to use a
     * built-in event rather than your own custom event.
     */
    def logLevelUpEvent(level: Option[Long]): Unit
    def logShareEvent(itemId: Option[String]): Unit
    def logGameOverEvent(score: Option[Long], map: Option[String]): Unit
    def logBeginTutorialEvent(): Unit
    def logCompleteTutorialEvent(): Unit
    /** post a score in the game
      *
      * post might be a bit misleading, this has nothing to do with
      * sharing or posting on social media, it is just about completing
      * a score (scoring a score) in the game.
      */
    def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit

    /** Log the visit of a new game screen */
    def logGameScreen(gameScreen: GameScreen): Unit
  }

  /** The entry point to the Analytics module
    *
    * Must be defined when wiring together the app.
    * Serves as a static, singleton, access point to the
    * analytics module configured.
    */
  val Analytics: Analytics

  /** Combine several analytics implementation
    *
    * Combine and dispatch events to several underlying analytics implementation.
    * This can be useful if data vizualization features from several
    * analytics services are commplementary, but in general
    * it would be better to just rely on one. Could also help during
    * a transition from one service to another one, but wishing to
    * maintain current analytics service for a while in order to be able
    * to have a history of data to compare against before performing the
    * switch. Or just while testing new analytics solutions.
    */
  class ParallelAnalytics(analytics: Seq[Analytics]) extends Analytics {
    override def logCustomEvent(name: String, params: EventParams): Unit = {
      analytics.foreach(_.logCustomEvent(name, params))
    }

    override def logLevelUpEvent(level: Option[Long]): Unit = {
      analytics.foreach(_.logLevelUpEvent(level))
    }
    override def logShareEvent(itemId: Option[String]): Unit = {
      analytics.foreach(_.logShareEvent(itemId))
    }
    override def logGameOverEvent(score: Option[Long], map: Option[String]): Unit = {
      analytics.foreach(_.logGameOverEvent(score, map))
    }
    override def logBeginTutorialEvent(): Unit = {
      analytics.foreach(_.logBeginTutorialEvent())
    }
    override def logCompleteTutorialEvent(): Unit = {
      analytics.foreach(_.logCompleteTutorialEvent())
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      analytics.foreach(_.logPostScoreEvent(score, level, character))
    }

    override def logGameScreen(gameScreen: GameScreen): Unit = {
      analytics.foreach(_.logGameScreen(gameScreen))
    }
  }

}


//TODO: EventParams might put pressure on the garbage collector as each custom event will need
//      to instantiate one of these

/** Optional parameters to an Event
  *
  * They provide semantics parameters for events. They cover
  * some common cases in game analytics.
  * 
  * itemId is a generic ID type that can identify the main
  * element in the Event.
  *
  * character is the identification of a character in the game
  *
  * level is not a map identifier, but the level of the player
  *
  * customs is for any extra, custom parameters
  */
case class EventParams(
  value: Option[Double] = None, level: Option[Long] = None,
  itemId: Option[String] = None, score: Option[Long] = None,
  map: Option[String] = None, character: Option[String] = None,
  customs: Map[String, String] = Map()
)
