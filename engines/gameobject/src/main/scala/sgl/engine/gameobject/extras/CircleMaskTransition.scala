package sgl.engine.gameobject.extras

import sgl.engine.gameobject.{Color, ComponentOf, GameSetup, GameContext, GameModule, Globals, RequiresGlobals, Scope, Stage, GameSystem, SystemId, TypedObject, World}
import sgl.engine.gameobject.render2d.{Painter2D, Render2D}
import sgl.math.Vec2

/** Covers the world with `color` except for a disc around `worldCenter`: an
  * iris transition when the radius is animated. It goes over everything drawn
  * in the world and under screen drawings. Needs [[CircleMaskModule]].
  */
final class CircleMaskTransition(
    obj: TypedObject[Any],
    var worldCenter: Vec2,
    var radius: Float,
    var color: Color = Color(0f, 0f, 0f, 1f),
    var segments: Int = 96,
    var visible: Boolean = true
) extends ComponentOf(obj) with RequiresGlobals {
  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[CircleMaskModule.type]
}

object CircleMaskSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.extras.circle-mask")

  override def run(context: GameContext)(using Scope): Unit =
    submit(context.world, context.global[Render2D])

  def submit(world: World, draw: Render2D): Unit =
    world.index[CircleMaskTransition].foreach { mask =>
      // In front of every other world drawing.
      if(mask.visible) draw.world(Float.PositiveInfinity)(paint(mask, _))
    }

  // A ring from the edge of the disc to well beyond the screen.
  private def paint(mask: CircleMaskTransition, painter: Painter2D): Unit = {
    val center = mask.worldCenter
    val reach = painter.viewport * 2f
    val segments = scala.math.max(12, mask.segments)
    def direction(i: Int): Vec2 = {
      val angle = (i.toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
      Vec2(scala.math.cos(angle).toFloat, -scala.math.sin(angle).toFloat)
    }
    var i = 0
    while(i < segments) {
      val d0 = direction(i)
      val d1 = direction(i + 1)
      val inner0 = center + d0 * mask.radius
      val inner1 = center + d1 * mask.radius
      val outer0 = center + Vec2(d0.x * reach.x, d0.y * reach.y)
      val outer1 = center + Vec2(d1.x * reach.x, d1.y * reach.y)
      painter.quad(inner0, outer0, outer1, inner1, mask.color)
      i += 1
    }
  }
}

/** Draws [[CircleMaskTransition]] components over the world. */
object CircleMaskModule extends GameModule {
  override def install(setup: GameSetup): Unit = {
    setup
      .setGlobal[CircleMaskModule.type](this)
      .addSystem(Stage.Render, CircleMaskSystem)
  }
}
