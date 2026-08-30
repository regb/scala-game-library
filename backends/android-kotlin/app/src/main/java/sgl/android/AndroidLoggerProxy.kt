package sgl.android

import android.util.Log
import sgl.proxy.LoggerProxy

class AndroidLoggerProxy(private val levelOrdinal: Int = Warning): LoggerProxy {
    companion object {
        const val NoLogging = -1
        const val Error = 0
        const val Warning = 1
        const val Info = 2
        const val Debug = 3
        const val Trace = 4
    }

    override fun logLevelOrdinal(): Int = levelOrdinal

    override fun error(tag: String?, msg: String?) {
        Log.e(tag ?: "sgl", msg ?: "")
    }

    override fun warning(tag: String?, msg: String?) {
        Log.w(tag ?: "sgl", msg ?: "")
    }

    override fun info(tag: String?, msg: String?) {
        Log.i(tag ?: "sgl", msg ?: "")
    }

    override fun debug(tag: String?, msg: String?) {
        Log.d(tag ?: "sgl", msg ?: "")
    }

    override fun trace(tag: String?, msg: String?) {
        Log.v(tag ?: "sgl", msg ?: "")
    }
}
