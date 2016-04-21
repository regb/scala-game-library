package sgl

/** Definitions to handle geometry and collisions
  *
  * We should move Point, Vec, Circle in this package
  * eventually. Probably everyhing should work with Double
  * for maximum precision, and because Int are a bit tricky
  * and often need to be converted in Double in middle.
  *
  * For example, a circle could be defined with Int pixel center, and Int
  * radius, but there will be some pixels that are only partially in the
  * area of the circle. Better to work with Double all the way.
  *
  * Double are also needed for Point and Vec, as part of the simulation of
  * objects. That is because some objects will not move 1 whole pixel during
  * intermediate frames, but we still need to accumulate the progress somehow,
  * hence the need for floating points.
  *
  * There is the question whether the GraphicsProvider should expose some interface
  * to draw geometry primitve like Rect or Circle, instead of taking individual coordinates.
  * It seems better to not mix geometry and GraphicsProvider, in order to not
  * impose the engine geometry system to users that only wishes to use the Graphics
  * for platform independence.
  */
package object geometry {

}
