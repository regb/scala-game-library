package sgl.engine.gameobject.spatial3d

import sgl.engine.gameobject.{Scope, Spatial3D}
import sgl.math.Mat4
import sgl.engine.gameobject.runtime.{GameBuilder, GameContext, GameModule}
import sgl.engine.gameobject.schedule.{Stage, System, SystemId}

object Spatial3DSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.spatial3d.update")

  override def run(context: GameContext)(using Scope): Unit = {
    context.world.index[Spatial3D].foreach { spatial =>
      if(spatial.parent.isEmpty) updateSubtree(spatial, Mat4.Identity)
    }
  }

  private def updateSubtree(spatial: Spatial3D, parentMatrix: Mat4): Unit = {
    val global = parentMatrix * spatial.local.matrix
    spatial.globalMatrixValue = global
    spatial.children.foreach(child => updateSubtree(child, global))
  }
}

object Spatial3DModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder.addSystem(Stage.PostUpdate, Spatial3DSystem)
  }
}
