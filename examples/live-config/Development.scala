package sgl.examples.liveconfig

import java.nio.file.Paths
import sgl.{CanvasProvider, WindowProvider}
import sgl.dev.{ConfigCodec, LiveConfig, PropertiesConfig}

trait Development extends AbstractApp {
  this: CanvasProvider with WindowProvider =>

  private lazy val config = new LiveConfig
  private lazy val speed = config.register("movement.speed", 100.0, ConfigCodec.double,
    (value: Double) => value >= 0 && value <= 2000)
  private lazy val adapter = {
    speed.onChange(value => println(s"Movement speed is now $value"))
    new PropertiesConfig(config, Paths.get(sys.env.getOrElse("SGL_LIVE_CONFIG_FILE", "tuning.properties")))
  }
  private var untilPoll = 0.0
  private var lastErrors = Vector.empty[String]

  override protected def movementSpeed: Double = speed.get

  override def frame(dt: Double): Unit = {
    untilPoll -= dt
    if(untilPoll <= 0) {
      untilPoll = 0.25
      adapter.poll() match {
        case Left(errors) =>
          if(errors != lastErrors) errors.foreach(Console.err.println)
          lastErrors = errors
        case Right(_) => lastErrors = Vector.empty
      }
    }
    super.frame(dt)
  }

  override def dispose(): Unit = {
    adapter.close()
    super.dispose()
  }
}
