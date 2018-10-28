package sgl
package analytics

/** The interface for the Analytics module.
  *
  * Analytics is used to instrument game code with user activity logging for
  * analysis. Contrary to the Logging module, the goal of Analytics is to
  * collect aggregate data in order to understand how players are playing the
  * game. While logging is extremely precise and enable developers to debug and
  * pin point a specific issue with the code, analytics are designed to be
  * looked at in aggregate and understand player's behaviours more globally.
  * Analytics will always be collected to a server for reporting, while logs
  * will generally stay at the client's site.
  *
  * The most common primitve for Analytics is the logging of events. Logging an
  * event can be seen as a structured form of logging, with an event id to
  * identify the event and well-defined fields (or parameters) to specify the
  * details of the event. Events are also very granular, as they store the
  * timestamp when the event occured, the player that triggered it, and the
  * exact values. Given a complete stream of events for a player, one can
  * essentially reconstruct the entire session of play of that player. Events
  * can also be looked at in aggregate, to understand how many times players
  * execute certain actions (that is, a count of all events), or an average
  * value for a score (e.g. taking the average of all the score params for a
  * game-over event).
  *
  * Having access to individual events is powerful as it gives the option to
  * look at a very detailed session, or to analyze data aggregated over a large
  * user base. The drawback to that flexibility is that events are extremely
  * verbose, and storing every single event for every single player can put a
  * lot of pressure on the backend.  This is when Metrics come into play.
  * Metrics are a snapshot of some relevant value.  An example of a metrics
  * could be time-played, the number of seconds that a user is playing the
  * game. This is data that is not always easy to represent using events, or
  * could be too expensive. One way to represent the time played as an event
  * would be to send an event every single second of play ("played one second")
  * and then using a count would give the total time. A smarter way is to send
  * a startplay and stopplay event with timestamp, but that then requires to do
  * some event value manipulation. A metric would always track the exact value
  * in memory, and regularly send a sample of the current value to the backend.
  *
  * Metrics are the most commonly used tool to monitor servers. This comes from
  * their very cheap cost of storage and manipulation, and the possibility to
  * do real-time analysis and slice and dice the data very quickly. While a
  * complete stream of discrete events contains essentially more information,
  * processing that stream of data into a curve that shows trends over time is
  * more expensive. That being said, most games analytics framework will only
  * offer custom events to their users, meaning that games will usually not
  * export metrics. That could be because most analysis of game data is done
  * offline (by opposition to live monitoring for a farm of servers) and the
  * amount of events could be low enough. One factor is that players usually
  * play a game for a few minutes, and then the game goes idle for a few hours
  * or days, while servers on the other hand never quite sleep and produces
  * data all the time.
  *
  * Nevertheless, using metrics still make sense to capture some game data. As
  * discussed above, time played is a very natural fit for a metrics, and so
  * are most performence related metrics (FPS, CPU usage, RAM usage, GC pause, etc).
  * This data might not always be available by all backend, but one can always
  * implement a custom backend where they would add support for this style of data.
  *
  * Player properties add another dimension to the analytics. A property has an
  * arbitrary name with an arbitrary value and they specify some propeties of a
  * player. Properties are set over the life of the session, according to actions
  * of the player. It is a way to classify the player base and analyze the actions
  * of each group. A player can have an arbitrary number of properties.
  *
  * Generally, the underlying implementation of the analytics will automatically log
  * a lot more events, metrics, and player properties. We do not attempt to expose
  * that in the API, as many of these will depend on the hooks available in the
  * platform. For example, Firebase on Android is able to populate the Player properties
  * with age and gender, because they have deep insights into the the system. Since
  * our game has no particular knowledge of these parameters, we will not provide
  * such API to the game. The goal of this Analytics module is to log as much
  * game-specific data as possible, and let the underlying implementation collect the rest.
  * 
  */
trait AnalyticsProvider { this: GameStateComponent =>

  /*
   * The design chosen here is to expose a very lightweight API to log
   * events, with a few built-in events that are common across many type
   * of games.
   *
   * We choose to just expose a list of functions, instead of an Event object,
   * for no particular reason other than having a simple, flat API. One
   * argument is that most event are created and logged at the same time, so it
   * does not make too much sense to create an event object and then pass
   * it to a log method. This is also better for the garbage collector, although
   * this could be worked around by statically creating all the events (without the
   * parameters) and having the parameters to be separated from the Event object.
   */

  abstract class Analytics {

    /** Log a generic custom event
      *
      * The name of the event will be used as the event ID for grouping in
      * the reporting dashboard. There are important considerations when
      * choosing a name. Two events with the same name are essentially the same
      * event, but triggered at different point in time with potentially different
      * parameters (and for potentially different users). In general, one should try
      * to limit the number of unique events to a constant (that is, not dynamically
      * generating the name).
      *
      * As an example, an event that a map was completed could have a name "complete-map"
      * and include a parameter for the mapid. Alternatively, if the game has a small and bounded
      * number of levels, one could consider generating the event name by including the map id
      * into the name, and being parameterless. Both designs have merits, and it does depend
      * on the kind of analysis that want to be done in the end, although both should
      * enable to query essentially the same data in the end.
      *
      * In general, the name should be a self-sufficient indication for the event, and the
      * parameters should be used as additional dimensions to observe the event on.
      */
    def logCustomEvent(name: String, params: EventParams): Unit

    /* log semantics events 
     * Some predefined events, the implementation will either
     * map them to a semantically corresponding event in the
     * analytics implementation, or with a reasonable custom event
     * Note that we don't provide default implementation with standard
     * name, as the naming convention will be dependendent of the platform
     * used for analytics. This is also why you should try to use a
     * built-in event rather than your own custom event.
     */

    /*
     * TODO: A custom event name could interfer with a default name used for one of
     *       the built-in event that we had to implement using the underlying custom
     *       event of the platform. One solution could be to export a function to 
     *       create an event as an object, using a name prefix, and then ensure in
     *       the backend that the actual names are unique.
     *       Providing a way to build the immutable unique event (without the params) once
     *       is also probably a good design, since logging it will then be cheaper (and
     *       we never have to garbage collect it).
     */

    /** Log an event that the player has leveled up.
      *
      * Typically used in games where the player gains XP points.  You can log
      * that event whenever the player goes from one level to the next. You
      * must provide the new level that the player reached. This event can
      * help to identify difficult phase in the game.
      */
    def logLevelUpEvent(level: Long): Unit


    /** Log an event when the player starts a new level.
      *
      * Identify the level with a string.
      */
    def logLevelStartEvent(level: String): Unit

    /** Log an event when the player starts a new level.
      *
      * Identify the level with a string. 
      */
    def logLevelEndEvent(level: String, success: Boolean): Unit

    def logShareEvent(itemId: Option[String]): Unit
    def logGameOverEvent(score: Option[Long], map: Option[String]): Unit
    def logBeginTutorialEvent(): Unit
    def logCompleteTutorialEvent(): Unit

    def logUnlockAchievementEvent(achievementId: String): Unit

    // TODO: currency typesafe.
    // def logIAPEvent(cartId: String, itemId: String, amount: Long, currency: String): Unit

    /** post a score in the game
      *
      * post might be a bit misleading, this has nothing to do with
      * sharing or posting on social media, it is just about completing
      * a score (scoring a score) in the game. The level param is the
      * current level of the player (or some other value reflecting the
      * level of the player, such as current stage, or total xp, as long
      * as it is used consistently).
      */
    def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit


    /** Set the current game screen.
      *
      * A game screen is usually a screen that is loaded only once
      * and that the player stays in for some time. For example, a player starts
      * in a menu screen, then transition into a level screen, then back
      * at the menu screen. Setting the current game screen helps tracking the
      * journey of the player through the app.
      *
      * In SGL, the game screen standard implementation should set that value, but
      * since the Analytics can be used in situations where the GameScreen module is not
      * used, the method is still exposed.
      */
    def setGameScreen(gameScreen: GameScreen): Unit

    /** Set a custom player property.
      *
      * A player property is associated to the player and will be
      * associated with each future event logged by this game. This
      * gives you a way to segment your players according to some
      * property and then analyze how each population behave.
      *
      * An example of a player property could be some settings chosen
      * by the player, such as the game difficulty, set them once
      * in the analytics and then have every event logged associated
      * with that property and filterable against it.
      */
    def setPlayerProperty(name: String, value: String): Unit

    // TODO: expose a Metrics API as well
    // class Metrics { ... }
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
    * This can be useful if data analysis and vizualization features from several
    * analytics services are complementary, but in general
    * it would be better to just rely on one. Could also help during
    * a transition from one service to another one, but wishing to
    * maintain current analytics service for a while in order to be able
    * to have a history of data to compare against before performing the
    * switch. Or just while testing new analytics solutions.
    */
  class MultipleAnalytics(analytics: Seq[Analytics]) extends Analytics {
    override def logCustomEvent(name: String, params: EventParams): Unit = {
      analytics.foreach(_.logCustomEvent(name, params))
    }

    override def logLevelUpEvent(level: Long): Unit = {
      analytics.foreach(_.logLevelUpEvent(level))
    }
    // TODO: level up as in player level but level levelStart as in map level..
    //       not good and we need to fix these names!
    override def logLevelStartEvent(level: String): Unit = {
      analytics.foreach(_.logLevelStartEvent(level))
    }
    override def logLevelEndEvent(level: String, success: Boolean): Unit = {
      analytics.foreach(_.logLevelEndEvent(level, success))
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
    override def logUnlockAchievementEvent(achievementId: String): Unit = {
      analytics.foreach(_.logUnlockAchievementEvent(achievementId))
    }
    override def logPostScoreEvent(score: Long, level: Option[Long], character: Option[String]): Unit = {
      analytics.foreach(_.logPostScoreEvent(score, level, character))
    }

    override def setGameScreen(gameScreen: GameScreen): Unit = {
      analytics.foreach(_.setGameScreen(gameScreen))
    }

    override def setPlayerProperty(name: String, value: String): Unit = {
      analytics.foreach(_.setPlayerProperty(name, value))
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
  * character is the identification of a character in the game.
  *
  * level is not a map identifier, but the level of the player.
  *
  * levelName is a map identifier.
  *
  * customs is for any extra, custom parameters
  */
case class EventParams(
  value: Option[Double] = None, level: Option[Long] = None,
  itemId: Option[String] = None, score: Option[Long] = None,
  levelName: Option[String] = None, character: Option[String] = None,
  customs: Map[String, String] = Map()
)
