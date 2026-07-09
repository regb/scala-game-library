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
  private val defaultFixedStep = 1.0 / 60.0

  def startup(): Unit = {
    world.resources.set(DeltaTime(0.0))
    world.resources.set(ElapsedTime(0.0))
    if(world.resources.getOption[FixedDeltaTime].isEmpty) {
      world.resources.set(FixedDeltaTime(defaultFixedStep))
    }
    run(Stage.Startup)
  }

  def frame(dt: Double): Unit = {
    elapsedSeconds += dt
    world.resources.set(DeltaTime(dt))
    world.resources.set(ElapsedTime(elapsedSeconds))

    run(Stage.PreUpdate)

    val fixedStep = world.resources.get[FixedDeltaTime].seconds
    fixedAccumulator += dt
    while(fixedAccumulator >= fixedStep && fixedStep > 0.0) {
      world.resources.set(DeltaTime(fixedStep))
      run(Stage.FixedUpdate)
      fixedAccumulator -= fixedStep
    }
    world.resources.set(DeltaTime(dt))

    run(Stage.Update)
    run(Stage.PostUpdate)
    run(Stage.RenderExtract)
    engine.renderer.render(world)
  }

  def resize(width: Int, height: Int): Unit =
    engine.renderer.resize(width, height)

  def dispose(): Unit = engine.dispose()

  private def run(stage: Stage): Unit =
    schedules.getOrElse(stage, Schedule.empty).run(world, engine)
}
