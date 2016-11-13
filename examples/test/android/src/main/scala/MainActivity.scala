package com.regblanc.sgl
package test.android

import android.app.Activity
import android.os.Bundle

import sgl.InputHelpersComponent
import sgl.android._
import test.core._
import sgl.util._

class MainActivity extends Activity with AbstractApp with AndroidApp
  with InputHelpersComponent with NoLoggingProvider {

  override val Fps = Some(40)

}
