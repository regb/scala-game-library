package sgl.engine.gameobject.render2d

import sgl.engine.gameobject.{Color, GameBuilder, GameContext, GameModule, Globals, RequiresGlobals, Scope, Stage, System, SystemId, World}
import sgl.math.Vec2

/** Mixed into the components that only do something with [[Render2DModule]]
  * installed: attaching one without it is an error instead of an object that
  * silently never appears.
  */
trait Rendered2D extends RequiresGlobals {
  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[Render2DModule.type]
}

// The built-in render systems use the same Render2D service a game does: they
// are the reference for writing one.

object SpriteRenderSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render2d.sprites")

  override def run(context: GameContext)(using Scope): Unit =
    submit(context.world, context.global[Render2D])

  def submit(world: World, draw: Render2D): Unit =
    world.index[SpriteRenderer].foreach { sprite =>
      draw.world(sprite.z)(paint(sprite, _))
    }

  private def paint(sprite: SpriteRenderer, painter: Painter2D): Unit = {
    val halfWidth = sprite.width / 2f
    val halfHeight = sprite.height / 2f
    def at(x: Float, y: Float): Vec2 = sprite.spatial.transformPoint(Vec2(x, y))

    sprite.visual match {
      case SpriteVisual.Texture(texture, u0, v0, u1, v1) =>
        painter.image(
          texture,
          at(-halfWidth, -halfHeight), at(halfWidth, -halfHeight), at(halfWidth, halfHeight), at(-halfWidth, halfHeight),
          u0, v0, u1, v1
        )
      case SpriteVisual.Triangle(color) =>
        painter.triangle(at(0f, -halfHeight), at(-halfWidth, halfHeight), at(halfWidth, halfHeight), color)
      case SpriteVisual.Circle(color, segments) =>
        val center = at(0f, 0f)
        var i = 0
        while(i < segments) {
          val a0 = (i.toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
          val a1 = ((i + 1).toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
          painter.triangle(
            center,
            at(scala.math.cos(a0).toFloat * halfWidth, scala.math.sin(a0).toFloat * halfWidth),
            at(scala.math.cos(a1).toFloat * halfWidth, scala.math.sin(a1).toFloat * halfWidth),
            color
          )
          i += 1
        }
    }
  }
}

object TilemapRenderSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.render2d.tilemaps")

  override def run(context: GameContext)(using Scope): Unit =
    submit(context.world, context.global[Render2D])

  def submit(world: World, draw: Render2D): Unit =
    world.index[Tilemap].foreach { tilemap =>
      draw.world(tilemap.z) { painter =>
        def at(x: Float, y: Float): Vec2 = tilemap.spatial.transformPoint(Vec2(x, y))
        // Quads are painted in order, which keeps overlapping translucent
        // tiles looking the way the map lists them.
        tilemap.quads.foreach { quad =>
          painter.image(
            quad.texture,
            at(quad.x, quad.y), at(quad.x + quad.width, quad.y),
            at(quad.x + quad.width, quad.y + quad.height), at(quad.x, quad.y + quad.height),
            quad.u0, quad.v0, quad.u1, quad.v1,
            Color(1f, 1f, 1f, quad.alpha)
          )
        }
      }
    }
}

/** Draws sprites and tilemaps, ordered by `z`, through the active
  * [[Camera2D]]. Its systems run in the Render stage.
  */
object Render2DModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder
      .setGlobal[Render2DModule.type](this)
      .addSystem(Stage.Render, SpriteRenderSystem)
      .addSystem(Stage.Render, TilemapRenderSystem)
  }
}
