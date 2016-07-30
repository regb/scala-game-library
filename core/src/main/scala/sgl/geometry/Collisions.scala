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

  private def projectsOnSegment(c: Point, ss: Point, se: Point): Boolean = {
    val s1 = (c - ss) * (se - ss)
    val s2 = (c - se) * (se - ss)
    s1*s2 < 0
  }

}
