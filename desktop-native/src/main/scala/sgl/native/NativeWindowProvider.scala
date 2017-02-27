package sgl
package native

trait NativeWindowProvider extends WindowProvider {
  this: GameStateComponent with NativeGraphicsProvider =>

  val frameDimension: (Int, Int)

  override def WindowWidth: Int = frameDimension._1
  override def WindowHeight: Int = frameDimension._2

  override def dpi: Int = 160

  override def density: Float = 1f

}
