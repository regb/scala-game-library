package sgl.geometry

object Collisions {

  def circleWithCircle(c1: Circle, c2: Circle): Boolean = {
    val dist = (c1.center - c2.center)
    val n2 = dist.x*dist.x + dist.y*dist.y
    n2 <= (c1.radius+c2.radius)*(c1.radius+c2.radius)
  }

  def aabbWithAabb(r1: Rect, r2: Rect): Boolean = {
    val noCollision = r2.left >= r1.left + r1.width ||
                      r2.left + r2.width <= r1.left ||
                      r2.top >= r1.top + r1.height ||
                      r2.top + r2.height <= r1.top
    !noCollision
  }

  def circleWithAabb(c: Circle, r: Rect): Boolean = {
    val circleAsRect = c.boundingRect
    if(!aabbWithAabb(circleAsRect, r)) {
      //no collision with overapproximation rect means for sure no collision
      false
    } else if(r.vertices.exists(p => c.intersect(p))) {
      //if one of the vertices of rect is in circle, we found a real collision
      true
    } else if(r.intersect(c.center)) {
      true
    } else {
      /* finally, there are two remaining cases. Either the circle intersects the
       * rectangle from one of the side, or it does not intersect at all.
       */
       val verticalProj = projectsOnSegment(c.center, r.topLeft, r.bottomLeft)
       val horizontalProj = projectsOnSegment(c.center, r.topLeft, r.topRight)
       if(verticalProj || horizontalProj)
         true
       else
         false
    }
  }

  /** Check collision of convex polygon versus polygon using Separated Axis Theorem.
    *
    * The SAT technique compares the shadows of each polygon along all axis
    * defined by all lines of both polygons, and if they don't overlap in any
    * of them, then the polygons do not intersect. This only works with convex
    * polygons.
    *
    * Axis are created by taking the normal of each line of each polygons.
    */
  def polygonWithPolygonSat(p1: Polygon, p2: Polygon): Boolean = {

    // Check for all axis of p1, that the shadows overlap.
    def check(p1: Polygon, p2: Polygon): Boolean = {
      for(i <- 0 until p1.nbEdges) {
        val a = p1.edgeStart(i)
        val b = p1.edgeEnd(i)
        val n = (a-->b).normal

        // We consider the axis defined by the normal to the line segment a->b.
        // We project all points to it to get a range for the shadow.

        var p1min = Float.MaxValue
        var p1max = Float.MinValue
        for(p <- p1.points) {
          val v = p.x*n.x + p.y*n.y
          p1min = p1min min v
          p1max = p1max max v
        }

        var p2min = Float.MaxValue
        var p2max = Float.MinValue
        for(p <- p2.points) {
          val v = p.x*n.x + p.y*n.y
          p2min = p2min min v
          p2max = p2max max v
        }

        // Finally, check if they overlap.
        if(p1min > p2max || p2min > p1max)
          return false
      }
      return true
    }
    check(p1, p2) && check(p2, p1)
  }

  private def projectsOnSegment(c: Point, ss: Point, se: Point): Boolean = {
    val s1 = (c - ss) * (se - ss)
    val s2 = (c - se) * (se - ss)
    s1*s2 < 0
  }

}
