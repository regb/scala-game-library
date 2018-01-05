package com.regblanc.scalavator
package desktop

import core._

import sgl._
import sgl.util._
import sgl.awt._
import sgl.awt.util._
import sgl.scene._


/** Wire backend to the App here */
object Main extends AbstractApp with AWTApp with SceneComponent with VerboseStdOutLoggingProvider with SaveComponent {

  override val TargetFps = Some(60)

  override val frameDimension = (400, 650)

  type Save = FileSave
  override val save: Save = new FileSave("./scalavator.save")

}
