package @PACKAGE@

import android.content.Context
import sgl.android.AndroidPlatformProxy
import sgl.android.AndroidSave
import sgl.android.AndroidSystemBarsBehavior
import sgl.android.AndroidSystemBarsMode
import sgl.android.BaseMainActivity
@OPTIONAL_IMPORTS@import sgl.proxy.ProxiedGameApp

fun makeGameApp(context: Context, platformProxy: AndroidPlatformProxy): ProxiedGameApp {
    return @WIRING_EXPRESSION@
}

class MainActivity : BaseMainActivity(::makeGameApp) {
    init {
        KeepScreenOn = @KEEP_SCREEN_ON@
        SystemBarsMode = @SYSTEM_BARS_MODE@
        SystemBarsBehavior = @SYSTEM_BARS_BEHAVIOR@
        NavigationBarContrastEnforced = @NAVIGATION_BAR_CONTRAST_ENFORCED@
    }
}
