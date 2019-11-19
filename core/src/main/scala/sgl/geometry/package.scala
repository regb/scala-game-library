package sgl

/** Definitions to handle geometry and collisions.
  *
  * All coordinates should be in Double and/or Float, This is both to support
  * world coordinates (expressed at Float-precision) and and because Int are a
  * bit tricky and often need to be converted in floating point in middle of an
  * operation.
  *
  * For example, a circle could be defined with Int pixel center, and Int
  * radius, but there will be some pixels that are only partially in the
  * area of the circle. Better to work with Double all the way.
  *
  * Double are also needed for Point and Vec, as part of the simulation of
  * physics. That is because some objects will not move 1 whole pixel during
  * intermediate frames, but we still need to accumulate the progress somehow,
  * hence the need for floating points.
  *
  * There is the question whether the GraphicsProvider should expose some interface
  * to draw geometry primitve like Rect or Circle, instead of taking individual coordinates.
  * It seems better to not mix geometry and GraphicsProvider, in order to not
  * impose the engine geometry system to users that only wishes to use the Graphics
  * for platform independence.
  *
  * Also, the axis system for the geometry follows the same axis system as the
  * rest of SGL, that is x is pointing right and y is pointing down (from
  * top-left to bottom-right). This might lead to some confusion because
  * standard math definition is to have y pointing up, and most algorithms here
  * are purely mathematical on geometric shapes. There's still more value in having
  * a consistent axis system in all of SGL though.
  */
package object geometry {

}
