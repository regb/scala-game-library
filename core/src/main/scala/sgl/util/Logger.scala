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

    private val errorPrefix = "[ Error ] "
    private val warningPrefix = "[Warning] "
    private val infoPrefix = "[ Info ] "
    private val debugPrefix = "[ Debug ] "
    private val tracePrefix = "[ Trace ] "

    def output(msg: String): Unit

    def logLevel: LogLevel

    protected def reline(prefix: String, tag: Tag, msg: String): String = {
      val colorPrefix =
        if(prefix == errorPrefix)
          Console.RED
        else if(prefix == warningPrefix)
          Console.YELLOW
        else if(prefix == debugPrefix)
          Console.MAGENTA
        else if(prefix == tracePrefix)
          Console.GREEN
        else //for INFO
          Console.BLUE
      val colorTag = Console.CYAN
      "[" + colorPrefix + prefix.substring(1, prefix.length-2) + Console.RESET + "] " +
      "[ " + colorTag + tag.name + Console.RESET + " ] " +
      msg.trim.replaceAll("\n", "\n" + (" " * (prefix.size)))
    }

    def error(msg: => String)(implicit tag: Tag) = if(logLevel >= Error) output(reline(errorPrefix, tag, msg))
    def warning(msg: => String)(implicit tag: Tag) = if(logLevel >= Warning) output(reline(warningPrefix, tag, msg))
    def info(msg: => String)(implicit tag: Tag) = if(logLevel >= Info) output(reline(infoPrefix, tag, msg))
    def debug(msg: => String)(implicit tag: Tag) = if(logLevel >= Debug) output(reline(debugPrefix, tag, msg))
    def trace(msg: => String)(implicit tag: Tag) = if(logLevel >= Trace) output(reline(tracePrefix, tag, msg))

  }

}

trait NoLoggingProvider extends LoggingProvider {
  object NoLogger extends Logger {
    import Logger._

    override def output(msg: String): Unit = {}

    override val logLevel = Error

    override def error(msg: => String)(implicit tag: Tag) = {}
    override def warning(msg: => String)(implicit tag: Tag) = {}
    override def info(msg: => String)(implicit tag: Tag) = {}
    override def debug(msg: => String)(implicit tag: Tag) = {}
    override def trace(msg: => String)(implicit tag: Tag) = {}
  }

  override val logger = NoLogger
}

trait StdErrLoggingProvider extends LoggingProvider {
  abstract class StdErrLogger extends Logger {
    override def output(msg: String): Unit = {
      Console.err.println(msg)
    }
  }
}

trait DefaultStdErrLoggingProvider extends StdErrLoggingProvider {
  val logger = DefaultStdErrLogger
  object DefaultStdErrLogger extends StdErrLogger {
    override val logLevel: Logger.LogLevel = Logger.Warning
  }
}

trait VerboseStdErrLoggingProvider extends StdErrLoggingProvider {
  val logger = VerboseStdErrLogger
  object VerboseStdErrLogger extends StdErrLogger {
    import Logger._
    override val logLevel: LogLevel = Debug
  }
}

trait TraceStdErrLoggingProvider extends StdErrLoggingProvider {
  val logger = TraceStdErrLogger
  object TraceStdErrLogger extends StdErrLogger {
    import Logger._
    override val logLevel: LogLevel = Trace
  }
}



trait StdOutLoggingProvider extends LoggingProvider {
  abstract class StdOutLogger extends Logger {
    override def output(msg: String): Unit = {
      Console.out.println(msg)
    }
  }
}

trait DefaultStdOutLoggingProvider extends StdOutLoggingProvider {
  val logger = DefaultStdOutLogger
  object DefaultStdOutLogger extends StdOutLogger {
    override val logLevel: Logger.LogLevel = Logger.Warning
  }
}

trait VerboseStdOutLoggingProvider extends StdOutLoggingProvider {
  val logger = VerboseStdOutLogger
  object VerboseStdOutLogger extends StdOutLogger {
    override val logLevel: Logger.LogLevel = Logger.Debug
  }
}

trait TraceStdOutLoggingProvider extends StdOutLoggingProvider {
  val logger = TraceStdOutLogger
  object TraceStdOutLogger extends StdOutLogger {
    override val logLevel: Logger.LogLevel = Logger.Trace
  }
}
