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

  def isEmpty: Boolean = size == 0

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
  }

  /** Starts a shape of that many vertices, each of which is then written at the
    * index returned by `next`.
    */
  def begin(texture: Option[T], vertexCount: Int): Unit = {
    val needed = size + vertexCount * floatsPerVertex
    if(needed > valuesArray.length) valuesArray = java.util.Arrays.copyOf(valuesArray, scala.math.max(needed, valuesArray.length * 2))
    if(runsCount == 0) startRun()
    // Most shapes have the texture of the one before.
    if(texture != lastTexture) {
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
    * Returns how many texture units were used.
    */
  def foreachRun(bind: (Int, T) => Unit)(draw: (Int, Int) => Unit): Int = {
    var used = 0
    var run = 0
    while(run < runsCount) {
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
    used
  }
}

private[sgl] object VertexBatch {
  /** Textures a draw call can mix. More would cost every pixel a longer choice. */
  val MaxSlots: Int = 8

  /** The fragment shader of batched shapes. It takes `v_color`, `v_uv` and a flat
    * `v_slot`, and reads the samplers `u_texture0` and so on.
    */
  def fragmentShader(header: String, slots: Int): String = {
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
      out vec4 out_color;

      void main() {
        int slot = int(v_slot + 0.5);
        vec4 texel = vec4(1.0);
        $branches
        out_color = texel * v_color;
      }
    """
  }
}
