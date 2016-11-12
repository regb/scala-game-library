package sgl
package awt

import javax.swing.JFrame
import javax.swing.JPanel

import java.awt.event._
import java.awt.Dimension

trait AWTWindowProvider extends WindowProvider with Lifecycle {
  this: GameStateComponent with ThreadBasedGameLoopProvider =>

  val frameTitle: String = "Default App"
  val frameDimension: Option[(Int, Int)] = None

  class ApplicationFrame(gamePanel: GamePanel) extends JFrame {
    
    this.setTitle(frameTitle)

    this.add(gamePanel)
    gamePanel.setFocusable(true)

    val (w, h) = frameDimension.getOrElse((400, 600))
    //this.setSize(w, h)
    this.getContentPane().setPreferredSize(new Dimension(w, h))
    gamePanel.setSize(w, h)
    this.pack()

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      override def windowClosing(windowEvent: WindowEvent): Unit = {
        gameLoop.stop()
      }
    })

    this.setVisible(true)

    this.setResizable(false)
    this.setLocationRelativeTo(null)
  }

  class GamePanel extends JPanel

  /*
   * We don't initialize as part of the cake mixin, because
   * of the usual issues with initialization order and null pointers
   * due to override (frameDimension). They are initialized in the main
   * instead
   */
  var gamePanel: GamePanel = null
  var applicationFrame: ApplicationFrame = null

  override def WindowWidth: Int = gamePanel.getWidth
  override def WindowHeight: Int = gamePanel.getHeight

  override def dpi: Int = 160

  override def density: Float = 1f

}
