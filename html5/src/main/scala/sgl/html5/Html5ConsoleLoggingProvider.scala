package sgl.html5

import sgl.util._

import org.scalajs.dom

/*
 * TODO: can we do nice colors like in the desktop terminal?
 */

trait Html5ConsoleLoggingProvider extends LoggingProvider {

  import Logger._

  abstract class ConsoleLogger extends Logger {
    override protected def reline(prefix: String, tag: Tag, msg: String): String = {
      val alignedMsg = msg.trim.replaceAll("\n", "\n" + (" " * prefix.length))
      s"${prefix}[ ${tag.name} ] $alignedMsg"
    }
    override def output(msg: String): Unit = dom.console.log(msg)
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
