package sgl.android

import android.content.Context

import sgl.proxy.GraphicsProxy
import sgl.proxy.PlatformProxy
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SchedulerProxy
import sgl.proxy.SystemProxy
import sgl.proxy.WindowProxy

class AndroidPlatformProxy(val context: Context, val gameView: GameView): PlatformProxy {
    override fun systemProxy(): SystemProxy {
        return AndroidSystemProxy()
    }

    override fun resourcesRoot(): ResourcePathProxy {
        return AndroidResourcePathProxy(listOf<String>())
    }

    override fun multiDPIResourcesRoot(): ResourcePathProxy {
        return AndroidResourcePathProxy(listOf<String>())
    }

    override fun windowProxy(): WindowProxy {
        return AndroidWindowProxy(gameView)
    }

    override fun graphicsProxy(): GraphicsProxy {
        return AndroidGraphicsProxy(context)
    }

    override fun schedulerProxy(): SchedulerProxy {
        return AndroidSchedulerProxy()
    }
}