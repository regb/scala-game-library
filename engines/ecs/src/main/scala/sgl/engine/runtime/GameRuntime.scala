package sgl.engine.runtime

import sgl.engine.schedule.{Schedule, Stage}
import sgl.engine.world.World

final class GameRuntime(
    val world: World,
    val engine: Engine,
    schedules: Map[Stage, Schedule]
) {
  private var elapsedSeconds: Double = 0.0
  private var fixedAccumulator: Double = 0.0
  private var started = false
  private var disposed = false
  private val defaultFixedStep = 1.0 / 60.0

  def startup(): Unit = {
    if(disposed) throw new IllegalStateException("Cannot start a disposed game runtime")
    if(started) throw new IllegalStateException("Game runtime has already been started")
    started = true
    world.resources.set(DeltaTime(0.0))
    world.resources.set(ElapsedTime(0.0))
    if(world.resources.getOption[FixedDeltaTime].isEmpty) {
      world.resources.set(FixedDeltaTime(defaultFixedStep))
    }
    run(Stage.Startup)
  }

  def frame(dt: Double): Unit = {
    ensureRunning()
    if(!dt.isFinite || dt < 0.0) throw new IllegalArgumentException("Frame delta time must be a finite, non-negative number of seconds")
    elapsedSeconds += dt
    world.resources.set(DeltaTime(dt))
    world.resources.set(ElapsedTime(elapsedSeconds))

    run(Stage.PreUpdate)

    val fixedStep = world.resources.get[FixedDeltaTime].seconds
    fixedAccumulator += dt
    if(fixedStep > 0.0) {
      while(fixedAccumulator >= fixedStep) {
        world.resources.set(DeltaTime(fixedStep))
        run(Stage.FixedUpdate)
        fixedAccumulator -= fixedStep
      }
    }
    world.resources.set(DeltaTime(dt))

    run(Stage.Update)
    run(Stage.PostUpdate)
    run(Stage.RenderExtract)
    engine.renderer.render(world)
  }

  def resize(width: Int, height: Int): Unit = {
    ensureRunning()
    engine.renderer.resize(width, height)
  }

  def dispose(): Unit = {
    if(disposed) return
    disposed = true
    world.dispose()
    engine.dispose()
  }

  private def ensureRunning(): Unit = {
    if(disposed) throw new IllegalStateException("Game runtime has been disposed")
    if(!started) throw new IllegalStateException("Game runtime has not been started")
  }

  private def run(stage: Stage): Unit =
    schedules.getOrElse(stage, Schedule.empty).run(world, engine)
}
