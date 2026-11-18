package sgl.engine.gameobject.render2d

import sgl.engine.gameobject.{Globals, RequiresGlobals, Scope}
import sgl.engine.gameobject.runtime.{GameBuilder, GameContext, GameModule}
import sgl.engine.gameobject.schedule.{Stage, System, SystemId}

/** Mixed into the components that only do something with [[Render2DModule]]
  * installed: attaching one without it is an error instead of an object that
  * silently never appears.
  */
trait Rendered2D extends RequiresGlobals {
  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[Render2DModule.type]
}

object SpriteRenderSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render2d.sprites")

  override def run(context: GameContext)(using Scope): Unit =
    context.engine.renderer.submitSprites(context.world)
}

object TilemapRenderSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render2d.tilemaps")

  override def run(context: GameContext)(using Scope): Unit =
    context.engine.renderer.submitTilemaps(context.world)
}

object ScreenEffectsRenderSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render2d.screen-effects")

  override def run(context: GameContext)(using Scope): Unit =
    context.engine.renderer.submitScreenEffects(context.world)
}

/** Draws the 2D components: sprites and tilemaps ordered by `z`, then circle
  * masks and text overlays on top. The systems run in the Render stage and
  * submit what they draw; the frame is composed when the stage ends, so their
  * order among other render systems does not matter.
  */
object Render2DModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder
      .setGlobal[Render2DModule.type](this)
      .addSystem(Stage.Render, SpriteRenderSystem)
      .addSystem(Stage.Render, TilemapRenderSystem)
      .addSystem(Stage.Render, ScreenEffectsRenderSystem)
  }
}
