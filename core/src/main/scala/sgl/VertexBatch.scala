package sgl

import scala.collection.mutable

/** Vertices of the shapes of a frame, to upload together and draw in as few
  * calls as possible. A draw call covers consecutive shapes as long as they
  * share a few textures.
  *
  * Shapes are never reordered, so what is in front and blending are unchanged.
  * Each vertex says which texture of its draw call it is for, its slot: 0 for
  * none, then from 1. The arrays grow to what a frame needs and are kept for
  * the next, so nothing is allocated for a shape.
  */
private[sgl] final class VertexBatch[T](val floatsPerVertex: Int, slots: Int) {
  private var valuesArray = new Array[Float](6 * floatsPerVertex * 1024)
  private var size = 0
  // Each run is drawn by one call. Its textures are the ones from its first up
  // to the first of the next, in slot order.
  private val textures = mutable.ArrayBuffer.empty[T]
  private var runsCount = 0
  private var runStarts = new Array[Int](64)
  private var runFirstTextures = new Array[Int](64)
  private var lastTexture: Option[T] = None
  private var currentSlot = 0f
  // What is done between two draw calls, and the run it is done before.
  private val actions = mutable.ArrayBuffer.empty[() => Unit]
  private val actionRuns = mutable.ArrayBuffer.empty[Int]
  // Whether the next shape starts a run, whatever its texture is.
  private var cut = false

  /** Whether there is nothing to draw and nothing to do. */
  def isEmpty: Boolean = size == 0 && actions.isEmpty

  /** The array vertices are written to, valid until the next `begin`. */
  def values: Array[Float] = valuesArray

  /** How many floats have been written. */
  def length: Int = size

  /** The slot of the texture of the shape being written. */
  def slot: Float = currentSlot

  def clear(): Unit = {
    size = 0
    runsCount = 0
    textures.clear()
    lastTexture = None
    currentSlot = 0f
    actions.clear()
    actionRuns.clear()
    cut = false
  }

  /** Does something after the shapes so far are drawn, and before the ones to
    * come, which are then drawn by another call.
    */
  def between(action: () => Unit): Unit = {
    actions += action
    actionRuns += runsCount
    cut = true
  }

  /** Starts a shape of that many vertices, each of which is then written at the
    * index returned by `next`.
    */
  def begin(texture: Option[T], vertexCount: Int): Unit = {
    val needed = size + vertexCount * floatsPerVertex
    if(needed > valuesArray.length) valuesArray = java.util.Arrays.copyOf(valuesArray, scala.math.max(needed, valuesArray.length * 2))
    if(runsCount == 0 || cut) {
      cut = false
      startRun()
      lastTexture = None
      currentSlot = 0f
    }
    // Most shapes have the texture of the one before.
    if((texture ne lastTexture) && texture != lastTexture) {
      lastTexture = texture
      texture match {
        case None => currentSlot = 0f
        case Some(wanted) =>
          var i = runFirstTextures(runsCount - 1)
          while(i < textures.size && textures(i) != wanted) i += 1
          if(i == textures.size) {
            if(i - runFirstTextures(runsCount - 1) == slots) startRun()
            textures += wanted
          }
          currentSlot = (i - runFirstTextures(runsCount - 1) + 1).toFloat
      }
    }
  }

  private def startRun(): Unit = {
    if(runsCount == runStarts.length) {
      runStarts = java.util.Arrays.copyOf(runStarts, runsCount * 2)
      runFirstTextures = java.util.Arrays.copyOf(runFirstTextures, runsCount * 2)
    }
    runStarts(runsCount) = size / floatsPerVertex
    runFirstTextures(runsCount) = textures.size
    runsCount += 1
  }

  def next(): Int = {
    val offset = size
    size += floatsPerVertex
    offset
  }

  /** Goes through the draw calls: `bind` is given each texture and its unit,
    * counted from 0, and `draw` then the first vertex and how many there are.
    * What is to be done in between is done in between. Returns how many texture
    * units were used by the calls.
    */
  def foreachRun(bind: (Int, T) => Unit)(draw: (Int, Int) => Unit): Int = {
    var used = 0
    var run = 0
    var action = 0
    while(run < runsCount) {
      while(action < actions.size && actionRuns(action) == run) {
        actions(action)()
        action += 1
      }
      val last = run + 1 == runsCount
      val first = runFirstTextures(run)
      val end = if(last) textures.size else runFirstTextures(run + 1)
      var i = first
      while(i < end) {
        bind(i - first, textures(i))
        i += 1
      }
      used = scala.math.max(used, end - first)
      val vertexEnd = if(last) size / floatsPerVertex else runStarts(run + 1)
      draw(runStarts(run), vertexEnd - runStarts(run))
      run += 1
    }
    while(action < actions.size) {
      actions(action)()
      action += 1
    }
    used
  }
}

private[sgl] object VertexBatch {
  /** Textures a draw call can mix. More would cost every pixel a longer choice. */
  val MaxSlots: Int = 8

  /** Added to the slot of vertices that are added to what is below them. */
  val AdditiveSlots: Float = 16f

  /** The fragment shader of batched shapes. It takes `v_color`, `v_uv` and a flat
    * `v_slot`, and reads the samplers `u_texture0` and so on.
    *
    * With `additive`, colors come out multiplied by their alpha, to be blended
    * with (One, OneMinusSrcAlpha): what it does depends on the alpha that is
    * left, so that shapes which cover and shapes which add share draw calls.
    *
    * An effect is the source of a function `vec4 effect(vec4 color, vec2 uv, vec2
    * position, vec4 params)` that changes the color. It takes `v_position`, and
    * the parameters in the second column of `u_effect`.
    */
  def fragmentShader(header: String, slots: Int, additive: Boolean = false, effect: Option[String] = None): String = {
    // Textures have no mipmaps in use, so naming the level changes nothing, and
    // it lets them be read in a branch.
    val samplers = (0 until slots).map(i => s"uniform sampler2D u_texture$i;").mkString("\n      ")
    val branches = (0 until slots)
      .map(i => s"if(slot == ${i + 1}) texel = textureLod(u_texture$i, v_uv, 0.0);").mkString("\n        else ")
    header + s"""

      in vec4 v_color;
      in vec2 v_uv;
      flat in float v_slot;
      $samplers
      ${if(effect.isEmpty) "" else "in vec2 v_position;\n      uniform mat4 u_effect;"}
      out vec4 out_color;
      ${effect.getOrElse("")}

      void main() {
        int slot = int(v_slot + 0.5);
        ${if(additive) s"float covers = slot >= ${AdditiveSlots.toInt} ? 0.0 : 1.0;\n        if(slot >= ${AdditiveSlots.toInt}) slot -= ${AdditiveSlots.toInt};" else ""}
        vec4 texel = vec4(1.0);
        $branches
        vec4 color = texel * v_color;
        ${if(effect.isEmpty) "" else "color = effect(color, v_uv, v_position, u_effect[1]);"}
        out_color = ${if(additive) "vec4(color.rgb * color.a, color.a * covers)" else "color"};
      }
    """
  }
}
