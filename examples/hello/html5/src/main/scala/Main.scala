package com.regblanc.sgl.test
package html5

import sgl.html5._
import sgl.html5.util._
import sgl.html5.themes._
import sgl._

object Main extends core.AbstractApp with Html5App 
  with InputHelpersComponent with Html5VerboseConsoleLoggingProvider {

  override val TargetFps = None

  override val GameCanvasID: String = "my_canvas"

  override val theme = new FullScreenTheme
  //override val theme = new DefaultTheme {
  //  override val maxFrame = (600, 600)
  //  override val optimalFrame = Some((600, 500))
  //}

}
