package sgl.engine.gameobject.spatial2d

import sgl.engine.gameobject.{ContextKey, Scope}
import sgl.engine.gameobject.runtime.{GameBuilder, GameContext, GameModule}
import sgl.engine.gameobject.schedule.{Stage, System, SystemId}

object Spatial2DSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.spatial2d.update")

  override def run(context: GameContext)(using Scope): Unit = {
    context.world.index[Spatial2D].foreach { spatial =>
      if(spatial.parent.isEmpty) updateSubtree(spatial, Transform2D.Identity)
    }
  }

  private def updateSubtree(spatial: Spatial2D, parent: Transform2D): Unit = {
    val global = parent.combine(spatial.local)
    spatial.globalValue = global
    spatial.children.foreach(child => updateSubtree(child, global))
  }
}

/** Keeps `Spatial2D.global` up to date. The module publishes itself in the
  * world context, so components that read global transforms can declare
  * `requiresContext[Spatial2DModule.type]`.
  */
object Spatial2DModule extends GameModule {
  given ContextKey[Spatial2DModule.type] = ContextKey[Spatial2DModule.type]("sgl.engine.gameobject.spatial2d.Spatial2DModule")

  override def install(builder: GameBuilder): Unit = {
    builder
      .setContext[Spatial2DModule.type](this)
      .addSystem(Stage.PostUpdate, Spatial2DSystem)
  }
}
