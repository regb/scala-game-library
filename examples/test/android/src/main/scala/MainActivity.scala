package com.regblanc.sgl
package test.android

import android.app.Activity
import android.os.Bundle

import sgl.{InputHelpersComponent, GameLoopStatisticsComponent}
import sgl.android._
import sgl.util._

import test.core._

class MainActivity extends Activity with AbstractApp with AndroidApp
  with InputHelpersComponent with NoLoggingProvider with GameLoopStatisticsComponent {

  override val Fps = Some(40)

}
