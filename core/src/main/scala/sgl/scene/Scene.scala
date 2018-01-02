package sgl
package scene

/** Component including all the scene features
  *
  * You can use this trait for easily depending on
  * all of the scene features, instead of importing
  * each individually.
  */
trait SceneComponent extends SceneGraphComponent {
  this: GraphicsProvider with InputProvider with SystemProvider =>

}
