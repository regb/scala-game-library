package sgl
package proxy

import sgl.util.LoggingProvider

trait ProxyLoggingProvider extends LoggingProvider {

  val PlatformProxy: PlatformProxy

  override val logger: Logger = new Logger {
    import Logger._

    override def logLevel: LogLevel = PlatformProxy.loggerProxy.logLevelOrdinal match {
      case i if i >= Trace.ordinal => Trace
      case i if i >= Debug.ordinal => Debug
      case i if i >= Info.ordinal => Info
      case i if i >= Warning.ordinal => Warning
      case i if i >= Error.ordinal => Error
      case _ => NoLogging
    }

    override protected def log(level: LogLevel, tag: Tag, msg: String): Unit = level match {
      case NoLogging => ()
      case Error => PlatformProxy.loggerProxy.error(tag.name, msg)
      case Warning => PlatformProxy.loggerProxy.warning(tag.name, msg)
      case Info => PlatformProxy.loggerProxy.info(tag.name, msg)
      case Debug => PlatformProxy.loggerProxy.debug(tag.name, msg)
      case Trace => PlatformProxy.loggerProxy.trace(tag.name, msg)
    }
  }
}
