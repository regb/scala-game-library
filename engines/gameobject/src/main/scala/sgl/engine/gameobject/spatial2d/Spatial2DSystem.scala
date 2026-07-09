package sgl.engine.gameobject.spatial2d

import sgl.engine.gameobject.{Scope, Spatial2D, Transform2D}
import sgl.engine.gameobject.runtime.{GameBuilder, GameModule}
import sgl.engine.gameobject.schedule.{Stage, System, SystemContext, SystemId}
import sgl.math.Vec2

object Spatial2DSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.spatial2d.update")

  override def run(context: SystemContext)(using Scope): Unit = {
    context.world.index[Spatial2D].foreach { spatial =>
      if(spatial.parent.isEmpty) updateSubtree(spatial, Transform2D.Identity)
    }
  }

  private def updateSubtree(spatial: Spatial2D, parent: Transform2D): Unit = {
    val local = spatial.local
    val global = Transform2D(
      position = parent.position + local.position,
      rotation = parent.rotation + local.rotation,
      scale = Vec2(parent.scale.x * local.scale.x, parent.scale.y * local.scale.y)
    )
    spatial.globalValue = global
    spatial.children.foreach(child => updateSubtree(child, global))
  }
}

object Spatial2DModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder.addSystem(Stage.PostUpdate, Spatial2DSystem)
  }
}
