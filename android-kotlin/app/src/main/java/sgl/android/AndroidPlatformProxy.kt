package sgl.android

import android.app.Activity
import android.content.Context
import sgl.proxy.AudioProxy

import sgl.proxy.GraphicsProxy
import sgl.proxy.PlatformProxy
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SchedulerProxy
import sgl.proxy.SystemProxy
import sgl.proxy.WindowProxy

class AndroidPlatformProxy(val context: Context, val gameView: GameView): PlatformProxy {
    
    // Cache all proxy instances to ensure state consistency
    private val _systemProxy: SystemProxy by lazy { 
        if (context !is Activity) {
            throw IllegalStateException("Context provided to AndroidPlatformProxy must be an Activity to initialize AndroidSystemProxy")
        }
        AndroidSystemProxy(context)
    }
    private val _resourcesRoot: ResourcePathProxy by lazy { AndroidResourcePathProxy(listOf<String>()) }
    private val _multiDPIResourcesRoot: ResourcePathProxy by lazy { AndroidResourcePathProxy(listOf<String>()) }
    private val _windowProxy: WindowProxy by lazy { AndroidWindowProxy(gameView) }
    private val _graphicsProxy: GraphicsProxy by lazy { AndroidGraphicsProxy(context) }
    private val _schedulerProxy: SchedulerProxy by lazy { AndroidSchedulerProxy() }
    private val _audioProxy: AudioProxy by lazy { AndroidAudioProxy(context) }
    
    override fun systemProxy(): SystemProxy {
        return _systemProxy
    }

    override fun resourcesRoot(): ResourcePathProxy {
        return _resourcesRoot
    }

    override fun multiDPIResourcesRoot(): ResourcePathProxy {
        return _multiDPIResourcesRoot
    }

    override fun windowProxy(): WindowProxy {
        return _windowProxy
    }

    override fun graphicsProxy(): GraphicsProxy {
        return _graphicsProxy
    }

    override fun schedulerProxy(): SchedulerProxy {
        return _schedulerProxy
    }

    override fun audioProxy(): AudioProxy {
        return _audioProxy
    }
}