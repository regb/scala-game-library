package sgl
package awt

import javax.swing.JFrame
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.JPanel

import java.awt.event._
import java.awt.Dimension
import java.awt.Toolkit
import java.awt

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

  class ApplicationFrame(canvas: awt.Canvas) extends JFrame {
    
    this.setTitle(frameTitle)

    // TODO: borderless, but no exit button.
    // this.setUndecorated(true)

    val (w, h) = frameDimension
    this.getContentPane().setPreferredSize(new Dimension(w, h))
    canvas.setSize(w, h)

    canvas.setFocusable(true)

    this.add(canvas, 0)
    this.pack()

    this.setDefaultCloseOperation(EXIT_ON_CLOSE)
  
    this.setVisible(true)

    this.setResizable(false)
    this.setLocationRelativeTo(null)

  }

  /*
   * We don't initialize as part of the cake mixin, because
   * of the usual issues with initialization order and null pointers
   * due to override (frameDimension). They are initialized in main
   * instead
   */
  var gameCanvas: awt.Canvas = null
  var applicationFrame: ApplicationFrame = null

  class AWTWindow extends AbstractWindow {

    override def width: Int = gameCanvas.getWidth
    override def height: Int = gameCanvas.getHeight

    /*
     * TODO: After doing some research, and trial and errors, it seems
     * like getting the screen ppi in Java is not very well supported. As
     * a temporary workaround, I export a settings to override the JVM
     * dpi with a constant chosen at compile time. This is motly helpful
     * for development in the local machine, to play around with different
     * PPI and also to make the game looks nice in case the JVM ppi is totally
     * out of whack with reality (as I've witnessed with a value of 95 provided
     * by the JVM while my actual PPI is about 200, which makes the game
     * unplayable).
     */
    override def xppi: Float = ScreenForcePPI.getOrElse(Toolkit.getDefaultToolkit().getScreenResolution().toFloat)
    override def yppi: Float = ScreenForcePPI.getOrElse(Toolkit.getDefaultToolkit().getScreenResolution().toFloat)
    override def ppi: Float = ScreenForcePPI.getOrElse(Toolkit.getDefaultToolkit().getScreenResolution().toFloat)

    override def logicalPpi: Float = ScreenForcePPI.getOrElse(Toolkit.getDefaultToolkit().getScreenResolution().toFloat)
  }
  type Window = AWTWindow
  override val Window = new AWTWindow

  /** Override this if you want to force an arbitrary PPI. */
  val ScreenForcePPI: Option[Float] = None
}
