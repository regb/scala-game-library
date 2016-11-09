package com.regblanc.sgl.test
package html5

import sgl.html5._
import sgl._
import sgl.util._

import scala.scalajs.js.annotation.JSExport

@JSExport
object Main extends core.AbstractApp with Html5App 
  with InputHelpersComponent with Html5VerboseConsoleLoggingProvider {

  override val CanvasDimension = Some((800, 400))

}
