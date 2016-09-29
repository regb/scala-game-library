package com.regblanc.scalavator

import android.app.Activity
import android.os.Bundle

import sgl.android._
import core._

import sgl.util._
import sgl.scene._

class MainActivity extends DefaultGameActivity {

  override def onCreate(bundle: Bundle) {
    gameApp = new ScalavatorApp(this)
    super.onCreate(bundle)
  }

  override def onPause(): Unit = {
    super.onPause()
  }

  override def onResume(): Unit = {
    super.onResume()
  }

}


/** Wire backend to the App here */
class ScalavatorApp(activity: MainActivity) 
  extends AbstractApp with AndroidApp with SceneComponent with NoLoggingProvider {

  override val Fps = Some(40)

  override val mainActivity = activity

}
