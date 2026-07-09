package sgl.android

import android.app.Activity
import android.content.Context
import sgl.proxy.AudioProxy
import sgl.proxy.GraphicsProxy
import sgl.proxy.JsonProxy
import sgl.proxy.LoggerProxy
import sgl.proxy.PlatformProxy
import sgl.proxy.SchedulerProxy
import sgl.proxy.SystemProxy
import sgl.proxy.WindowProxy

class AndroidPlatformProxy(val context: Context, val gameView: GameView): PlatformProxy {
    private val _systemProxy: SystemProxy by lazy {
        if (context !is Activity) {
            throw IllegalStateException("Context provided to AndroidPlatformProxy must be an Activity to initialize AndroidSystemProxy")
        }
        AndroidSystemProxy(context)
    }
    private val _windowProxy: WindowProxy by lazy { AndroidWindowProxy(gameView) }
    private val _graphicsProxy: GraphicsProxy by lazy { AndroidGraphicsProxy(context) }
    private val _schedulerProxy: SchedulerProxy by lazy { AndroidSchedulerProxy() }
    private val _audioProxy: AudioProxy by lazy { AndroidAudioProxy(context) }
    private val _loggerProxy: LoggerProxy by lazy { AndroidLoggerProxy() }
    private val _jsonProxy: JsonProxy by lazy { AndroidJsonProxy() }

    override fun systemProxy(): SystemProxy = _systemProxy
    override fun resourcesRoot(): String = ""
    override fun multiDPIResourcesRoot(): String = ""
    override fun windowProxy(): WindowProxy = _windowProxy
    override fun graphicsProxy(): GraphicsProxy = _graphicsProxy
    override fun schedulerProxy(): SchedulerProxy = _schedulerProxy
    override fun audioProxy(): AudioProxy = _audioProxy
    override fun loggerProxy(): LoggerProxy = _loggerProxy
    override fun jsonProxy(): JsonProxy = _jsonProxy
}
