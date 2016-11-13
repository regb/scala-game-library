package sgl.html5

import sgl.util._

import org.scalajs.dom

/*
 * Note that it seems scala.js does nicely eliminate all overhead
 * of logging call when using NoLoggingProvider (the empty logger).
 * It is nice, as we pass everything by value, the string are not
 * even constructed so the logging framework is basically free
 * in production.
 */

trait Html5ConsoleLoggingProvider extends LoggingProvider {

  import Logger._

  abstract class ConsoleLogger extends Logger {
    private def format(tag: Tag, msg: String): String = {
      val prefix = s"[ ${tag.name} ]"
      val alignedMsg = msg.trim.replaceAll("\n", "\n" + (" " * prefix.length))
      s"${prefix} $alignedMsg"
    }

    override protected def log(level: LogLevel, tag: Tag, msg: String): Unit = level match {
      case NoLogging => ()
      case Error => dom.console.error(format(tag, msg))
      case Warning => dom.console.warn(format(tag, msg))
      case Info => dom.console.info(format(tag, msg))
      case Debug => dom.console.log(format(tag, msg))
      case Trace => dom.console.log(format(tag, msg))
    }
  }
}


trait Html5DefaultConsoleLoggingProvider extends Html5ConsoleLoggingProvider {
  case object DefaultConsoleLogger extends ConsoleLogger {
    override val logLevel: Logger.LogLevel = Logger.Warning
  }
  override val logger = DefaultConsoleLogger
}

trait Html5InfoConsoleLoggingProvider extends Html5ConsoleLoggingProvider {
  case object InfoConsoleLogger extends ConsoleLogger {
    override val logLevel: Logger.LogLevel = Logger.Info
  }
  override val logger = InfoConsoleLogger
}

trait Html5VerboseConsoleLoggingProvider extends Html5ConsoleLoggingProvider {
  case object VerboseConsoleLogger extends ConsoleLogger {
    override val logLevel: Logger.LogLevel = Logger.Debug
  }
  override val logger = VerboseConsoleLogger
}
