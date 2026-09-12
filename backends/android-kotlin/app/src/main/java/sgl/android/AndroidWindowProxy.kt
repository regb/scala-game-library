package sgl.android

import sgl.proxy.WindowProxy

class AndroidWindowProxy(val gameView: GameView): WindowProxy {
    override fun width(): Int {
        return gameView.width
    }

    override fun height(): Int {
        return gameView.height
    }

    override fun safeAreaInsetLeft(): Int = gameView.safeAreaInsets.left()
    override fun safeAreaInsetTop(): Int = gameView.safeAreaInsets.top()
    override fun safeAreaInsetRight(): Int = gameView.safeAreaInsets.right()
    override fun safeAreaInsetBottom(): Int = gameView.safeAreaInsets.bottom()

    override fun xppi(): Float {
        return gameView.resources.displayMetrics.xdpi
    }

    override fun yppi(): Float {
        return gameView.resources.displayMetrics.ydpi
    }

    override fun logicalPpi(): Float {
        return gameView.resources.displayMetrics.densityDpi.toFloat()
    }
}