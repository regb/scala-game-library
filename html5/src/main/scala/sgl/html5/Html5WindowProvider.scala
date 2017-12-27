package sgl
package html5

trait Html5WindowProvider extends WindowProvider {
  this: Html5App =>

  override def WindowWidth: Int = this.htmlCanvas.width
  override def WindowHeight: Int = this.htmlCanvas.height

  override def dpi: Int = 160

  override def density: Float = 1f

}
