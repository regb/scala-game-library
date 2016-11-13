package sgl
package html5

trait Html5WindowProvider extends WindowProvider with Lifecycle {
  this: GameStateComponent with Html5GraphicsProvider =>

  abstract override def startup(): Unit = {
    super.startup()
  }

  val CanvasDimension: Option[(Int, Int)] = None

  override def WindowWidth: Int = this.canvas.width
  override def WindowHeight: Int = this.canvas.height

  override def dpi: Int = 160

  override def density: Float = 1f

}
