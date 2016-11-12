package com.regblanc.sgl
package test.android

import android.app.Activity
import android.os.Bundle

import sgl._
import sgl.android._
import test.core._
import sgl.util._

class MainActivity extends DefaultGameActivity {
  override def onCreate(bundle: Bundle) {
    gameApp = new SGLTestApp(this)
    super.onCreate(bundle)
  }
}

/** Wire backend to the App here */
class SGLTestApp(activity: MainActivity) 
  extends AbstractApp with AndroidApp with NoLoggingProvider with InputHelpersComponent {

  override val Fps = Some(40)

  override val mainActivity = activity

}
