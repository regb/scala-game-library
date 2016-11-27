package com.regblanc.scalavator
package html5

import sgl._
import sgl.scene._
import sgl.html5._
import sgl.html5.themes._
import sgl.util._
import sgl.html5.util._

import scala.scalajs.js.annotation.JSExport

@JSExport
object Main extends core.AbstractApp with Html5App 
  with Html5VerboseConsoleLoggingProvider
  //with NoLoggingProvider
  with SceneComponent with InputHelpersComponent {

  //We should not force the fps on Html5 and just let
  //requestAnimationFrame do its best
  override val Fps: Option[Int] = None

  override val theme = new DefaultTheme {
    override val maxFrame = (400, 650)
  }

}
