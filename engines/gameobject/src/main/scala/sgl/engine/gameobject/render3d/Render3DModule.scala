package sgl.engine.gameobject.render3d

import sgl.engine.gameobject.{GameBuilder, GameContext, GameModule, Globals, RequiresGlobals, Scope, Stage, GameSystem, SystemId}

/** Mixed into the components that only do something with [[Render3DModule]]
  * installed: attaching one without it is an error instead of an object that
  * silently never appears.
  */
trait Rendered3D extends RequiresGlobals {
  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[Render3DModule.type]
}

object MeshRenderSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render3d.meshes")

  override def run(context: GameContext)(using Scope): Unit =
    context.engine.renderer.drawMeshes(context.world)
}

/** Draws the meshes from the active [[Camera3D]], in the Render stage. */
object Render3DModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder
      .setGlobal[Render3DModule.type](this)
      .addSystem(Stage.Render, MeshRenderSystem)
  }
}
