package sgl.android.util

import sgl.util._

import android.util.Log

/** Logging provider using android.util.Log
  *
  * Provide logging by delegating it to the standard android library
  * android.util.Log
  */
trait AndroidLoggingProvider extends LoggingProvider {

  import Logger._

  abstract class LogLogger extends Logger {
    override protected def log(level: LogLevel, tag: Tag, msg: String): Unit = level match {
      case NoLogging => ()
      case Error => Log.e(tag.name, msg)
      case Warning => Log.w(tag.name, msg)
      case Info => Log.i(tag.name, msg)
      case Debug => Log.d(tag.name, msg)
      case Trace => Log.v(tag.name, msg)
    }
  }
}


trait AndroidDefaultLoggingProvider extends AndroidLoggingProvider {
  case object DefaultLogLogger extends LogLogger {
    override val logLevel: Logger.LogLevel = Logger.Warning
  }
  override val logger = DefaultLogLogger
}

trait AndroidVerboseLoggingProvider extends AndroidLoggingProvider {
  case object VerboseLogLogger extends LogLogger {
    override val logLevel: Logger.LogLevel = Logger.Debug
  }
  override val logger = VerboseLogLogger
}

trait AndroidTracingLoggingProvider extends AndroidLoggingProvider {
  case object TracingLogLogger extends LogLogger {
    override val logLevel: Logger.LogLevel = Logger.Trace
  }
  override val logger = TracingLogLogger
}
