package sgl.engine.gameobject.extras

import sgl.engine.gameobject.{Color, ComponentOf, GameSetup, GameContext, GameModule, Globals, RequiresGlobals, Scope, Stage, GameSystem, SystemId, TypedObject, World}
import sgl.engine.gameobject.render2d.{Painter2D, Render2D}
import sgl.geometry.Vec2

// A drawable written the way a game would write its own: a component, a
// system in the Render stage submitting to Render2D, and a module. Nothing
// here uses more of the engine than a game can.

/** Lines of text in a box, in screen pixels: enough for a score or a debug
  * readout. It uses a built-in 5x7 uppercase font. Needs [[TextOverlayModule]].
  */
final class TextOverlay(
    obj: TypedObject[Any],
    var lines: Vector[String],
    var visible: Boolean = true,
    var x: Float = 32f,
    var y: Float = 32f,
    var scale: Float = 5f,
    var background: Color = Color(0f, 0f, 0f, 0.78f),
    var color: Color = Color(1f, 1f, 1f, 1f),
    var order: Int = 0
) extends ComponentOf(obj) with RequiresGlobals {
  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[TextOverlayModule.type]
}

object TextOverlaySystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.extras.text-overlay")

  override def run(context: GameContext)(using Scope): Unit =
    submit(context.world, context.global[Render2D])

  def submit(world: World, draw: Render2D): Unit =
    world.index[TextOverlay].foreach { overlay =>
      if(overlay.visible && overlay.lines.nonEmpty) draw.screen(overlay.order)(paint(overlay, _))
    }

  private def paint(overlay: TextOverlay, painter: Painter2D): Unit = {
    val charWidth = 5f * overlay.scale
    val charHeight = 7f * overlay.scale
    val gap = overlay.scale
    val lineGap = overlay.scale * 2f
    val pad = overlay.scale * 4f
    val width = overlay.lines.map(_.length).max.toFloat * (charWidth + gap) + pad * 2f
    val height = overlay.lines.size.toFloat * (charHeight + lineGap) - lineGap + pad * 2f

    def rect(x: Float, y: Float, w: Float, h: Float, color: Color): Unit =
      painter.quad(Vec2(x, y), Vec2(x + w, y), Vec2(x + w, y + h), Vec2(x, y + h), color)

    rect(overlay.x, overlay.y, width, height, overlay.background)
    overlay.lines.zipWithIndex.foreach { case (line, row) =>
      line.toUpperCase.zipWithIndex.foreach { case (char, column) =>
        glyph(char).zipWithIndex.foreach { case (bits, glyphY) =>
          var glyphX = 0
          while(glyphX < 5) {
            if(((bits >> (4 - glyphX)) & 1) == 1) {
              rect(
                overlay.x + pad + column * (charWidth + gap) + glyphX * overlay.scale,
                overlay.y + pad + row * (charHeight + lineGap) + glyphY * overlay.scale,
                overlay.scale, overlay.scale, overlay.color
              )
            }
            glyphX += 1
          }
        }
      }
    }
  }

  // One Int per row, top to bottom; the five low bits are the pixels.
  private def glyph(char: Char): Vector[Int] = char match {
    case 'A' => Vector(14,17,17,31,17,17,17); case 'B' => Vector(30,17,17,30,17,17,30)
    case 'C' => Vector(14,17,16,16,16,17,14); case 'D' => Vector(30,17,17,17,17,17,30)
    case 'E' => Vector(31,16,16,30,16,16,31); case 'F' => Vector(31,16,16,30,16,16,16)
    case 'G' => Vector(14,17,16,23,17,17,14); case 'H' => Vector(17,17,17,31,17,17,17)
    case 'I' => Vector(14,4,4,4,4,4,14); case 'J' => Vector(7,2,2,2,18,18,12)
    case 'K' => Vector(17,18,20,24,20,18,17); case 'L' => Vector(16,16,16,16,16,16,31)
    case 'M' => Vector(17,27,21,21,17,17,17); case 'N' => Vector(17,25,21,19,17,17,17)
    case 'O' => Vector(14,17,17,17,17,17,14); case 'P' => Vector(30,17,17,30,16,16,16)
    case 'Q' => Vector(14,17,17,17,21,18,13); case 'R' => Vector(30,17,17,30,20,18,17)
    case 'S' => Vector(15,16,16,14,1,1,30); case 'T' => Vector(31,4,4,4,4,4,4)
    case 'U' => Vector(17,17,17,17,17,17,14); case 'V' => Vector(17,17,17,17,17,10,4)
    case 'W' => Vector(17,17,17,21,21,21,10); case 'X' => Vector(17,17,10,4,10,17,17)
    case 'Y' => Vector(17,17,10,4,4,4,4); case 'Z' => Vector(31,1,2,4,8,16,31)
    case '0' => Vector(14,17,19,21,25,17,14); case '1' => Vector(4,12,4,4,4,4,14)
    case '2' => Vector(14,17,1,2,4,8,31); case '3' => Vector(30,1,1,14,1,1,30)
    case '4' => Vector(2,6,10,18,31,2,2); case '5' => Vector(31,16,16,30,1,1,30)
    case '6' => Vector(14,16,16,30,17,17,14); case '7' => Vector(31,1,2,4,8,8,8)
    case '8' => Vector(14,17,17,14,17,17,14); case '9' => Vector(14,17,17,15,1,1,14)
    case '-' => Vector(0,0,0,31,0,0,0); case '_' => Vector(0,0,0,0,0,0,31)
    case ':' => Vector(0,4,4,0,4,4,0); case '.' => Vector(0,0,0,0,0,12,12)
    case '[' => Vector(14,8,8,8,8,8,14); case ']' => Vector(14,2,2,2,2,2,14)
    case '/' => Vector(1,1,2,4,8,16,16); case ' ' => Vector(0,0,0,0,0,0,0)
    case _ => Vector(31,17,1,2,4,0,4)
  }
}

/** Draws [[TextOverlay]] components over the frame. */
object TextOverlayModule extends GameModule {
  override def install(setup: GameSetup): Unit = {
    setup
      .setGlobal[TextOverlayModule.type](this)
      .addSystem(Stage.Render, TextOverlaySystem)
  }
}
