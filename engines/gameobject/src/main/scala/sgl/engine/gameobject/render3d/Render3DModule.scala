package sgl.engine.gameobject.render3d

import sgl.engine.gameobject.{GameSetup, GameContext, GameModule, Globals, RequiresGlobals, Scope, Stage, GameSystem, SystemId, World}

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
    submit(context.world, context.global[Render3D])

  def submit(world: World, draw: Render3D): Unit =
    world.index[MeshRenderer].foreach { renderer =>
      draw.mesh(renderer.mesh, renderer.material, renderer.spatial.globalMatrix)
    }
}

/** Draws the meshes from the active [[Camera3D]], in the Render stage. */
object Render3DModule extends GameModule {
  override def install(setup: GameSetup): Unit = {
    setup
      .setGlobal[Render3DModule.type](this)
      .addSystem(Stage.Render, MeshRenderSystem)
  }
}
