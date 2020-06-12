package com.regblanc.sgl.board
package desktop

import core._

import sgl.ViewportComponent
import sgl.native._
import sgl.native.util._

/** Wire backend to the App here */
object Main extends AbstractApp with NativeApp
  with TraceStdErrLoggingProvider
  with ViewportComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (450, 780)

}
