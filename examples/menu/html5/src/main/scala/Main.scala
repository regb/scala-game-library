package com.regblanc.sgl.menu
package html5

import sgl.html5._
import sgl.html5.util._
import sgl.html5.themes._
import sgl._
import sgl.scene._
import sgl.scene.ui._

object Main extends Html5App with core.AbstractApp
  with InputHelpersComponent with Html5VerboseConsoleLoggingProvider
  with SceneComponent with PopupsComponent with ViewportComponent {

  override val TargetFps = None

  override val GameCanvasID: String = "my_canvas"

  override val theme = new DefaultTheme {
    override val maxFrame = (400, 600)
  }

}
