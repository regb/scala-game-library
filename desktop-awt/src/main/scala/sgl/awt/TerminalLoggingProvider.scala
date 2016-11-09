package sgl
package awt

import util._

/*
 * These are default loggers that make sense in the JVM/Desktop
 * environment. They are not necessarly only for AWT, but until
 * we get more platforms they should at least be independant from
 * the core library since they assume the existence of a terminal.
 * They also use some color code to display nice color messages.
 */

trait TerminalLoggingProvider extends LoggingProvider {
  import Logger._

  private val ErrorPrefix = "[ Error ] "
  private val WarningPrefix = "[Warning] "
  private val InfoPrefix = "[ Info ] "
  private val DebugPrefix = "[ Debug ] "
  private val TracePrefix = "[ Trace ] "
  
  abstract class TerminalLogger extends Logger {
    def output(msg: String): Unit

    protected override def log(level: LogLevel, tag: Tag, msg: String): Unit = level match {
      case NoLogging => ()
      case Error => output(reline(ErrorPrefix, tag, msg))
      case Warning => output(reline(WarningPrefix, tag, msg))
      case Info => output(reline(InfoPrefix, tag, msg))
      case Debug => output(reline(DebugPrefix, tag, msg))
      case Trace => output(reline(TracePrefix, tag, msg))
    }

    private def reline(prefix: String, tag: Tag, msg: String): String = {
      val colorPrefix =
        if(prefix == ErrorPrefix)
          Console.RED
        else if(prefix == WarningPrefix)
          Console.YELLOW
        else if(prefix == DebugPrefix)
          Console.MAGENTA
        else if(prefix == TracePrefix)
          Console.GREEN
        else //for INFO
          Console.BLUE
      val colorTag = Console.CYAN
      "[" + colorPrefix + prefix.substring(1, prefix.length-2) + Console.RESET + "] " +
      "[ " + colorTag + tag.name + Console.RESET + " ] " +
      msg.trim.replaceAll("\n", "\n" + (" " * (prefix.size)))
    }
  }

}
trait StdErrLoggingProvider extends TerminalLoggingProvider {
  abstract class StdErrLogger extends TerminalLogger {
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



trait StdOutLoggingProvider extends TerminalLoggingProvider {
  abstract class StdOutLogger extends TerminalLogger {
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

// vim: set ts=4 sw=4 et:
