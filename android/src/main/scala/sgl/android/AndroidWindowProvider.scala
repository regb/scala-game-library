package sgl
package android

import _root_.android.app.Activity
import _root_.android.view.SurfaceView

trait AndroidWindowProvider extends WindowProvider {


  val mainActivity: DefaultGameActivity

  override def height = mainActivity.gameView.getHeight
  override def width = mainActivity.gameView.getWidth

  private val BaseDensity: Double = 160
  override def dpi: Int = mainActivity.gameView.getResources.getDisplayMetrics.densityDpi
  override def density: Float = mainActivity.gameView.getResources.getDisplayMetrics.density

}
