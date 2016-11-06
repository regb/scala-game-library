package sgl
package html5

trait Html5WindowProvider extends WindowProvider with Lifecycle {
  this: GameStateComponent =>

  abstract override def startup(): Unit = {
    super.startup()
  }

  val CanvasDimension: Option[(Int, Int)] = None

  override def WindowWidth: Int = ???
  override def WindowHeight: Int = ???

  override def dpi: Int = 160

  override def density: Float = 1f

}
