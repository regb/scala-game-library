package com.regblanc.sgl.snake
package html5

import sgl._
import sgl.scene._
import sgl.html5._
import sgl.html5.themes._
import sgl.util._
import sgl.html5.util._

import scala.scalajs.js.annotation.JSExport

object Main extends Html5App with core.AbstractApp 
  with Html5VerboseConsoleLoggingProvider {

  override val GameCanvasID: String = "my_canvas"

  //We should not force the fps on Html5 and just let
  //requestAnimationFrame do its best
  override val TargetFps: Option[Int] = None

  override val theme = new FixedWindowTheme {
    override val frameSize = (TotalWidth, TotalHeight)
  }

}
