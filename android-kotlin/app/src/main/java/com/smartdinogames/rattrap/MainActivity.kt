package com.smartdinogames.rattrap

import android.content.Context
import android.os.Bundle
import sgl.android.AndroidPlatformProxy
import sgl.android.AndroidSave
import sgl.android.BaseMainActivity
import sgl.android.GameView
import sgl.android.analytics.AndroidFirebaseAnalytics
import sgl.proxy.ProxiedGameApp

fun makeGameApp(context: Context, platformProxy: AndroidPlatformProxy): ProxiedGameApp {
    //return com.regblanc.sgl.snake.core.Wiring.wire(AndroidPlatformProxy(context, gameView))
    //return com.regblanc.sgl.test.core.Wiring.wire(AndroidPlatformProxy(context, gameView))
    return com.smartdinogames.rattrap.Wiring.wire(
        platformProxy,  
        AndroidSave("rattrap", context),
        AndroidFirebaseAnalytics(context)
    )
}

class MainActivity : BaseMainActivity(::makeGameApp) {

}
