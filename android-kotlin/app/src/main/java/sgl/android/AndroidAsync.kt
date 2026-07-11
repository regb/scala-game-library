package sgl.android

import sgl.util.DefaultLoader
import sgl.util.Loader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object AndroidAsync {
    private val executor: ExecutorService = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "sgl-android-loader").apply { isDaemon = true }
    }

    fun <A> loader(body: () -> A): Loader<A> {
        val promise = DefaultLoader<A>()
        executor.execute {
            try {
                promise.success(body())
            } catch (throwable: Throwable) {
                promise.failure(throwable)
            }
        }
        return promise.loader()
    }
}
