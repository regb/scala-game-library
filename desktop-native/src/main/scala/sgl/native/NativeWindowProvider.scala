package sgl
package native

trait NativeWindowProvider extends WindowProvider {
  this: GameStateComponent with NativeGraphicsProvider =>

  override def WindowWidth: Int = 500//this.canvas.width
  override def WindowHeight: Int = 500//this.canvas.height

  override def dpi: Int = 160

  override def density: Float = 1f

}
