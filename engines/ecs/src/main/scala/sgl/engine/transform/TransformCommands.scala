package sgl.engine.transform

import sgl.engine.ecs.Entity
import sgl.engine.schedule.Commands

object TransformCommands {
  extension (commands: Commands) {
    /** Parent `child` under `parent` in the transform hierarchy. */
    def setParent(child: Entity.Id, parent: Entity.Id): Unit =
      commands.relate[Parent](source = child, target = parent)

    /** Remove any transform parent from `child`. */
    def clearParent(child: Entity.Id): Unit =
      commands.removeRelation[Parent](child)
  }
}
