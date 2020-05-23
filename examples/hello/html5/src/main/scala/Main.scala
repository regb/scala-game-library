package com.regblanc.sgl.test
package html5

import sgl.html5._
import sgl.html5.util._
import sgl.html5.themes._
import sgl._

import sgl.html5.analytics._

object Main extends core.AbstractApp with Html5App 
  with InputHelpersComponent with Html5VerboseConsoleLoggingProvider with Html5FirebaseAnalyticsProvider {

  override val TargetFps = None

  override val GameCanvasID: String = "my_canvas"

  override val theme = new DefaultTheme {
    override val maxFrame = (800, 800)
  }

  //Analytics.logCustomEvent("heythere", sgl.analytics.EventParams())
  //Analytics.logLevelUpEvent(27)
  //Analytics.setPlayerProperty("a", "b")
  //Analytics.logPostScoreEvent(42, None, Some("abcd"))
  //Analytics.logCustomEvent("my_custom_event", sgl.analytics.EventParams(itemId = Some("itema"), customs = Map("x" -> "y", "x2" -> "y2")))
}
