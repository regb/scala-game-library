package com.regblanc.sgl
package test.android

import android.app.Activity
import android.os.Bundle

import sgl.GameLoopStatisticsComponent
import sgl.android._
import sgl.util._

import test.core._

class MainActivity extends Activity with AbstractApp with AndroidApp
  with NoLoggingProvider with GameLoopStatisticsComponent {

  override val TargetFps = Some(40)

}
