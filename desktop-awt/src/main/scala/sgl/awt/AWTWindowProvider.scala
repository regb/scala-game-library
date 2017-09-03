package sgl
package awt

import javax.swing.JFrame
import javax.swing.JPanel

import java.awt.event._
import java.awt.Dimension

trait AWTWindowProvider extends WindowProvider {
  this: GameStateComponent =>

  /** The title of the frame */
  val frameTitle: String = "Default App"

  /** The dimension of the game window
    *
    * This is the exact dimension of the rendering canvas area.
    * The full frame will contain a header with some cross button
    * and its size will depend on the system (linux mac windows), and
    * so it will be slighlty higher than the dimension specified here
    * and will vary from system to system. But the playable area is going
    * to have a consistent size.
    */
  val frameDimension: (Int, Int)

  class ApplicationFrame(gamePanel: GamePanel) extends JFrame {
    
    this.setTitle(frameTitle)

    this.add(gamePanel)
    gamePanel.setFocusable(true)

    val (w, h) = frameDimension
    this.getContentPane().setPreferredSize(new Dimension(w, h))
    gamePanel.setSize(w, h)
    this.pack()

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  
    this.setVisible(true)

    this.setResizable(false)
    this.setLocationRelativeTo(null)
  }

  class GamePanel extends JPanel

  /*
   * We don't initialize as part of the cake mixin, because
   * of the usual issues with initialization order and null pointers
   * due to override (frameDimension). They are initialized in main
   * instead
   */
  var gamePanel: GamePanel = null
  var applicationFrame: ApplicationFrame = null

  override def WindowWidth: Int = gamePanel.getWidth
  override def WindowHeight: Int = gamePanel.getHeight

  override def dpi: Int = 160

  override def density: Float = 1f

}
