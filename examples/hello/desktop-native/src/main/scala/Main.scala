package com.regblanc.sgl.hello
package desktop

import core._

import sgl.native._
import sgl.native.util._

/** Wire native backend to the Hello app here. */
object Main extends NativeApp with AbstractApp
  with VerboseStdErrLoggingProvider {

  override val TargetFps = Some(60)

  override val frameDimension = (800, 800)

  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector("examples", "hello", "assets"))
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector("examples", "hello", "assets", "drawable-mdpi"))
}
