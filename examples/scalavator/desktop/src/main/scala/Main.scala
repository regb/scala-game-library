package com.regblanc.scalavator
package desktop

import core._

import sgl.awt._
import sgl.util._
import sgl.scene._


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp with SceneComponent with VerboseStdOutLoggingProvider {

  override val Fps = Some(60)

  override val frameDimension = Some((400, 650))

}
