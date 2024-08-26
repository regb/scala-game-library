package com.regblanc.sgl
package menu.android

import android.app.Activity
import android.os.Bundle

import sgl.{GameLoopStatisticsComponent, ViewportComponent}
import sgl.android._
import sgl.util._
import sgl.scene._

import test.core._

class MainActivity extends Activity with AbstractApp with AndroidApp
  with NoLoggingProvider with GameLoopStatisticsComponent
  with SceneComponent with ViewportComponent {

  override val TargetFps = Some(40)

}
