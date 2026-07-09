package sgl.engine.runtime

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.opengl.OpenGLEngine
import sgl.util.LoggingProvider

trait EngineApp extends Application {

  private var runtime: GameRuntime = _

  def configure(builder: GameBuilder): Unit
  protected def createEngine(): Engine

  final override def create(): Unit = {
    val builder = new GameBuilder
    configure(builder)
    runtime = builder.build(createEngine())
    runtime.startup()
  }

  final override def frame(dt: Double): Unit =
    runtime.frame(dt)

  override def resize(width: Int, height: Int): Unit =
    if(runtime != null) runtime.resize(width, height)

  override def dispose(): Unit =
    if(runtime != null) runtime.dispose()
}

trait OpenGLEngineApp extends EngineApp
    with OpenGLProvider
    with SystemProvider
    with WindowProvider
    with LoggingProvider {

  override protected def createEngine(): Engine = new OpenGLEngine(this)
}
