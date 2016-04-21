package sgl
package awt

import javax.swing.JFrame
import javax.swing.JPanel

import java.awt.event._

trait AWTWindowProvider extends WindowProvider with Lifecycle {
  this: GameLoopComponent =>

  abstract override def startup(): Unit = {
    applicationFrame.setVisible(true)
    super.startup()
  }

  val frameTitle: String = "Default App"
  val frameDimension: Option[(Int, Int)] = None

  class ApplicationFrame(gamePanel: GamePanel) extends JFrame {
    
    this.setTitle(frameTitle)

    val (w, h) = frameDimension.getOrElse((400, 600))
    this.setSize(w, h)
    this.setResizable(false)

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    this.setLocationRelativeTo(null)
  
    this.add(gamePanel)
    gamePanel.setFocusable(true)
  
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      override def windowClosing(windowEvent: WindowEvent): Unit = {
        gameLoop.stop()
      }
    })
  }

  class GamePanel extends JPanel

  lazy val gamePanel = new GamePanel
  lazy val applicationFrame = new ApplicationFrame(gamePanel)

  override def width: Int = gamePanel.getWidth
  override def height: Int = gamePanel.getHeight

  override def dpi: Int = 160

  override def density: Float = 1f

}
