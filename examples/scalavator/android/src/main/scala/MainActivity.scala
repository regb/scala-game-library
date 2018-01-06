package com.regblanc.scalavator

import android.app.Activity
import android.os.Bundle

import core._

import sgl._
import sgl.android._
import sgl.util._
import sgl.scene._

class MainActivity extends Activity with AndroidApp with AbstractApp
  with SceneComponent with NoLoggingProvider with SaveComponent {

  override val TargetFps = Some(30)

  type Save = AndroidSave
  override val save = new AndroidSave("scalavator-savefile", this)

}
