package sgl.geometry

class Polygon(val vertices: Vector[Vec]) {
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

  // TODO: Currently we assume that we have convex polygons, as our only
  // collision detection method for polygons need convex polygons. We should
  // add code to detect convex/concave polygons, and then some way to
  // do triangulation in order to transform a concave polygon into a set of
  // convex polygons that we can then apply our collision algorithm on.
  //   - https://en.wikipedia.org/wiki/Polygon_triangulation
  //   - https://math.stackexchange.com/questions/1743995/determine-whether-a-polygon-is-convex-based-on-its-vertices
  //   - https://stackoverflow.com/questions/471962/how-do-i-efficiently-determine-if-a-polygon-is-convex-non-convex-or-complex
}

object Polygon {
  def apply(vertices: Vector[Vec]) = new Polygon(vertices)
}
