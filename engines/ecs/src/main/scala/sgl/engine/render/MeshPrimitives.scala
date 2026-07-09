package sgl.engine.render

object MeshPrimitives {
  def sphere(segments: Int, rings: Int): MeshData = {
    val vertices = scala.collection.mutable.ArrayBuffer.empty[Float]
    var ring = 0
    while(ring <= rings) {
      val v = ring.toFloat / rings.toFloat
      val theta = v * scala.math.Pi
      val y = scala.math.cos(theta).toFloat
      val radius = scala.math.sin(theta).toFloat
      var segment = 0
      while(segment <= segments) {
        val u = segment.toFloat / segments.toFloat
        val phi = u * scala.math.Pi * 2.0
        val x = (scala.math.cos(phi) * radius).toFloat
        val z = (scala.math.sin(phi) * radius).toFloat
        vertices += x; vertices += y; vertices += z
        vertices += x; vertices += y; vertices += z
        vertices += u; vertices += (1f - v)
        segment += 1
      }
      ring += 1
    }

    val indices = scala.collection.mutable.ArrayBuffer.empty[Short]
    ring = 0
    while(ring < rings) {
      var segment = 0
      while(segment < segments) {
        val a = ring * (segments + 1) + segment
        val b = a + segments + 1
        indices += a.toShort; indices += (a + 1).toShort; indices += b.toShort
        indices += (a + 1).toShort; indices += (b + 1).toShort; indices += b.toShort
        segment += 1
      }
      ring += 1
    }

    MeshData(vertices.toArray, indices.toArray)
  }
}
