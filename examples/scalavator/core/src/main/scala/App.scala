package com.regblanc.scalavator
package core

import sgl._
import scene._
import util._

trait AbstractApp extends MainScreenComponent with LoadingScreenComponent {
  this: GraphicsProvider with InputProvider with WindowProvider with AudioProvider
  with GameStateComponent with SystemProvider
  with SceneComponent with LoggingProvider with SaveComponent =>

}
