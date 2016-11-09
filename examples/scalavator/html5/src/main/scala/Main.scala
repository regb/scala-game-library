package com.regblanc.scalavator
package html5

import sgl._
import sgl.scene._
import sgl.html5._
import sgl.util._

import scala.scalajs.js.annotation.JSExport

@JSExport
object Main extends core.AbstractApp with Html5App 
  //with Html5VerboseConsoleLoggingProvider
  with NoLoggingProvider
  with SceneComponent with InputHelpersComponent {

  override val CanvasDimension = Some((400, 650))

}
