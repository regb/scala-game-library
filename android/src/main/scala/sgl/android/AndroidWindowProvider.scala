package sgl
package android

import _root_.android.app.Activity
import _root_.android.view.SurfaceView

trait AndroidWindowProvider extends WindowProvider {

  var gameView: GameView = null

  override def WindowHeight = gameView.getHeight
  override def WindowWidth = gameView.getWidth

  private val BaseDensity: Double = 160
  override def dpi: Int = gameView.getResources.getDisplayMetrics.densityDpi
  override def density: Float = gameView.getResources.getDisplayMetrics.density

}
