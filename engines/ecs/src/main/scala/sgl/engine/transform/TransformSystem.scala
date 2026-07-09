package sgl.engine.transform

import sgl.engine.ecs.Entity
import sgl.math.Mat4
import sgl.engine.runtime.GameModule
import sgl.engine.schedule.{Stage, System, SystemContext}

object TransformSystem extends System {
  override val id = sgl.engine.schedule.SystemId("sgl.engine.transform.update")

  override def run(context: SystemContext): Unit = {
    val world = context.world
    val parentRelations = world.relations[Parent]

    world.query[LocalTransform].foreach { (entity, _) =>
      if(parentRelations.targetOf(entity).isEmpty) {
        updateSubtree(context, entity, Mat4.Identity)
      }
    }
  }

  private def updateSubtree(context: SystemContext, entity: Entity.Id, parentMatrix: Mat4): Unit = {
    val world = context.world
    world.getOption[LocalTransform](entity).foreach { local =>
      val global = parentMatrix * local.matrix
      context.commands.set(entity, GlobalTransform(global))
      world.relations[Parent].sourcesOf(entity).foreach { child =>
        updateSubtree(context, child, global)
      }
    }
  }
}

object TransformModule extends GameModule {
  override def install(builder: sgl.engine.runtime.GameBuilder): Unit = {
    builder
      .registerComponent[LocalTransform]
      .registerComponent[GlobalTransform]
      .registerRelation[Parent]
      .addSystem(Stage.PostUpdate, TransformSystem)
  }
}
