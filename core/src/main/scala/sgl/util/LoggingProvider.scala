package sgl.util

import scala.annotation.implicitNotFound

/** The interface for providing the Logging system.
  *
  * The logging system is intended for plain-text debugging information that
  * will be mainly used while developing the game. In particular, this means
  * that when building a release version of the game, it is totally acceptable
  * to inject a NoLoggingProvider implementation to entirely skipped logging. Given
  * that the dependencies are known at compile time, there should be no overhead in
  * logging in a release build, so you are encouraged to use as much detailed logging
  * as possible.
  *
  * The available log level are:
  *
  *   - Error, when something went seriously wrong. In general, the game should quit
  *     when an error is detected, although it could try to recover and continue if
  *     it has access to enough state. A release build should hopefully never contain
  *     an error, and any error should be eventually fixed.
  *
  *   - Warning, when something unusual happened that we would prefer not to but that
  *     the game can handle with usually some degraded user experience. One example would
  *     be the FPS dropping under the target FPS of the game. Ideally a release build should
  *     not ever have any warning, although the game can probably work with a few of them.
  *
  *   - Info. These are expected events that are worth notifying about. Things like reaching
  *     a new level are example of info level. Info logs should be understandable by most people
  *     reading them, even if not intimately familiar with the code base. Info logs can still be
  *     technical, but of a high enough level that some developers not familiar with the project
  *     would still be able to make sense of it.
  *
  *   - Debug. These are low level information, mostly related to the details of the game
  *     loop. These logs should only be useful to developers on the project itself. These logs
  *     should be extensively used during the development of the game itself, and can be left
  *     in the code for the release, by injecting a logging provider that ignore these logs.
  *
  *   - Trace. These are essentially more detailed logs than Debug. Typically, Debug should
  *     be enough to trace most issues, but one could look at Trace when they need to have
  *     even more details on what is happening. An example of tracing logs could be logging
  *     each entry and exit of functions. Also, as a general methodology, most events that
  *     should be logged on each frame probably should be of trace level.
  *
  * It is conceivable for a release build to include logging for Info/Warning/Error levels. The
  * argument there is to play nicely with the rest of the system, by logging few but important
  * events from the game point of view, giving a chance to the system owner (the player, but some
  * might be savy) to know that something is wrong and to potentially fix it by tuning their system
  * or by reporting it to the game developers. However, Debug and Trace level logging should never
  * be shipped in a release build, because they cost some overhead and they require knowledge of the
  * code base to fix anyway. One potentially interesting strategy for a release build would be to
  * keep the recent log in memory (not actually logging it) and send it to the developer (maybe
  * using a system feature) only when an error is encountered. That would provide the developer
  * with a detailed log session until the error.
  *
  * Outside of SGL, logging sometimes encompass more than just plain-text debugging. In particular,
  * there is a notion of structured logging, with seveeral dimensions and used for more structured
  * debugging and analytics. Similarly to structured logging, we also sometimes refer to logging
  * of events, which can then be queried for extensive analytics. In SGL, we use Logging solely for
  * plain-text debugging (not analytics) and defer the tools for analytics to the Analytics
  * module, which provides a way to log structured events.
  */
trait LoggingProvider {

  object Logger {

    sealed trait LogLevel extends Ordered[LogLevel] {
      val ordinal: Int

      override def compare(ll: LogLevel): Int = ordinal - ll.ordinal
    }
    object NoLogging extends LogLevel {
      override val ordinal = -1
    }
    object Error extends LogLevel {
      override val ordinal = 0
    }
    object Warning extends LogLevel {
      override val ordinal = 1
    }
    object Info extends LogLevel {
      override val ordinal = 2
    }
    object Debug extends LogLevel {
      override val ordinal = 3
    }
    object Trace extends LogLevel {
      override val ordinal = 4
    }

    @implicitNotFound("No implicit logger tag found in scope. You need define an implicit util.Logger.Tag")
    case class Tag(val name: String)
  }


  val logger: Logger

  abstract class Logger {

    import Logger._

    /** The log level this Logger displays
      *
      * The ordering is based on the importance, meaning
      * that the Error level is always shown if a lower
      * importance level is shown as well. As such, this is
      * the most verbose level at which log should still
      * be displayed
      */
    def logLevel: LogLevel


    // TODO: Maybe the log function should be public and the logLevel
    //       could be NoLOgging (unlike the comment below). Hiding it
    //       does not seem to bring much, and there are use cases where
    //       the clients want to log differently the same line given
    //       some current conditions (that are not just the logger config).
    //       One such case is recursive search function, which would like
    //       to log top level recursion but the more deep they go the less
    //       verbose they should be due to how unreadable the logs would
    //       become.

    /** Output the string with given level and tag
      *
      * The level is the importance of the message, and should never be 
      * NoLogging. The level is always a visible level, so no need
      * to check again the logLevel threashold
      */
    protected def log(level: LogLevel, tag: Tag, msg: String): Unit

    def error(msg: => String)(implicit tag: Tag) = if(logLevel >= Error) log(Error, tag, msg)
    def warning(msg: => String)(implicit tag: Tag) = if(logLevel >= Warning) log(Warning, tag, msg)
    def info(msg: => String)(implicit tag: Tag) = if(logLevel >= Info) log(Info, tag, msg)
    def debug(msg: => String)(implicit tag: Tag) = if(logLevel >= Debug) log(Debug, tag, msg)
    def trace(msg: => String)(implicit tag: Tag) = if(logLevel >= Trace) log(Trace, tag, msg)

  }

}

trait NoLoggingProvider extends LoggingProvider {

  object SilentLogger extends Logger {
    import Logger._

    override def log(level: LogLevel, tag: Tag, msg: String): Unit = ()

    override val logLevel = NoLogging

    override def error(msg: => String)(implicit tag: Tag) = {}
    override def warning(msg: => String)(implicit tag: Tag) = {}
    override def info(msg: => String)(implicit tag: Tag) = {}
    override def debug(msg: => String)(implicit tag: Tag) = {}
    override def trace(msg: => String)(implicit tag: Tag) = {}
  }

  override val logger = SilentLogger
}

