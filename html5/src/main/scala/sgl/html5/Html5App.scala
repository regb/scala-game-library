package sgl
package html5

import util._

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import dom.html

trait Html5App extends GameApp 
                  with Html5GraphicsProvider with Html5InputProvider with Html5AudioProvider
                  with Html5WindowProvider with Html5GameLoopProvider with Html5SystemProvider {

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    println("Hello world!")

    CanvasDimension.foreach(p => {
      val width = p._1
      val height = p._2
      canvas.width  = width
      canvas.height = height
    })

    this.canvas = canvas

    this.registerInputListeners()
    this.startGameLoop()

    this.startup()
    this.resume()

    //val ctx = canvas.getContext("2d").asInstanceOf[Ctx2D]
    //ctx.clearRect(0, 0, 800, 800)
    //ctx.strokeStyle = "black"
    //ctx.lineWidth = 6
    //ctx.rect(0, 0, 50, 50)
    //ctx.fillRect(100, 100, 50, 50)
    //ctx.stroke()
  }

}
