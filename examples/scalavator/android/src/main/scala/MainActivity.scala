package com.regblanc.scalavator

import android.app.Activity
import android.os.Bundle

import sgl.android._
import core._

import sgl.util._
import sgl.scene._
import sgl.Lifecycle

class MainActivity extends Activity with AbstractApp with AndroidApp
  with SceneComponent with NoLoggingProvider {

  override val Fps = Some(40)

}
