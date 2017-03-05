package sgl
package native

trait NativeWindowProvider extends WindowProvider {
  this: GameStateComponent with NativeGraphicsProvider =>

  val frameDimension: (Int, Int)

  //TODO: provide a WindowDimension object, with either fixed width/height or FullScreen
  //abstract class WindowDimension
  //case class FixedWindowDimension(width: Int, height: Int)
  //case object FullScreen
  //case class ResizableWIndowDimension(width: Int, height: Int)
  //val WindowDimension: WindowDimension

  override def WindowWidth: Int = frameDimension._1
  override def WindowHeight: Int = frameDimension._2

  override def dpi: Int = 160

  override def density: Float = 1f

}
