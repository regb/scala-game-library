package @PACKAGE@

import android.content.Context
import sgl.android.AndroidPlatformProxy
import sgl.android.BaseMainActivity
import sgl.proxy.ProxiedGameApp

fun makeGameApp(context: Context, platformProxy: AndroidPlatformProxy): ProxiedGameApp {
    return @PACKAGE@.core.Wiring.wire(platformProxy)
}

class MainActivity : BaseMainActivity(::makeGameApp)
