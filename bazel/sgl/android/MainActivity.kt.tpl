package @PACKAGE@

import android.content.Context
import sgl.android.AndroidPlatformProxy
import sgl.android.AndroidSave
import sgl.android.BaseMainActivity
@OPTIONAL_IMPORTS@import sgl.proxy.ProxiedGameApp

fun makeGameApp(context: Context, platformProxy: AndroidPlatformProxy): ProxiedGameApp {
    return @WIRING_EXPRESSION@
}

class MainActivity : BaseMainActivity(::makeGameApp)
