package com.regblanc.sgl.menu
package desktop

import core._

import sgl.native._
import sgl.native.util._

/** Wire native backend to the Menu app here. */
object Main extends NativeApp with AbstractApp
  with VerboseStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (400, 600)
}
