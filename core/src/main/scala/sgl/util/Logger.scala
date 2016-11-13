package sgl.util

import scala.annotation.implicitNotFound

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

