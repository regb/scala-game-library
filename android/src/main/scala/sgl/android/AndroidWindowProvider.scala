package sgl
package android

import _root_.android.app.Activity
import _root_.android.view.SurfaceView

trait AndroidWindowProvider extends WindowProvider {


  val mainActivity: DefaultGameActivity

  override def WindowHeight = mainActivity.gameView.getHeight
  override def WindowWidth = mainActivity.gameView.getWidth

  private val BaseDensity: Double = 160
  override def dpi: Int = mainActivity.gameView.getResources.getDisplayMetrics.densityDpi
  override def density: Float = mainActivity.gameView.getResources.getDisplayMetrics.density

}
