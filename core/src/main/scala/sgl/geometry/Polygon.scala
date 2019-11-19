package sgl.geometry

class Polygon(val vertices: Array[Vec]) {
  require(vertices.size > 2)

  def nbEdges: Int = vertices.size

  def edgeStart(i: Int): Vec = vertices(i)
  def edgeEnd(i: Int): Vec = vertices((i+1) % vertices.size)

  def boundingBox: Rect = {
    var top: Float = Float.MaxValue
    var bottom: Float = Float.MinValue
    var left: Float = Float.MaxValue
    var right: Float = Float.MinValue

    for(v <- vertices) {
      top = top min v.y
      bottom = bottom max v.y
      left = left min v.x
      right = right max v.x
    }

    Rect.fromBoundingBox(left=left, top=top, right=right, bottom=bottom)
  }
}

object Polygon {
  def apply(vertices: Array[Vec]) = new Polygon(vertices)
}
