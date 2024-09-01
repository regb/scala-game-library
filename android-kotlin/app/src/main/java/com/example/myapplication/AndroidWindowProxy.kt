package sgl.android

import sgl.proxy.WindowProxy

class AndroidWindowProxy(val gameView: GameView): WindowProxy {
    override fun width(): Int {
        return gameView.width
    }

    override fun height(): Int {
        return gameView.height
    }

    override fun xppi(): Float {
        return gameView.resources.displayMetrics.xdpi
    }

    override fun yppi(): Float {
        return gameView.resources.displayMetrics.ydpi
    }

    override fun ppi(): Float {
        return gameView.resources.displayMetrics.densityDpi.toFloat()
    }

    override fun logicalPpi(): Float {
        return gameView.resources.displayMetrics.densityDpi.toFloat()
    }
}