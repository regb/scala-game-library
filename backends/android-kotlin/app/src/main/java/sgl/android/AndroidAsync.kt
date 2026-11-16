package sgl.android

import sgl.util.DefaultLoader
import sgl.util.Loader
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal object AndroidAsync {
    /** A few threads, not one per resource: games load dozens of assets at once at startup, and
     * an unbounded pool started a thread for each (68 seen in an ANR report on a low-end phone),
     * all decoding bitmaps together and starving the main thread. Idle threads end after 30s. */
    private val threads = (Runtime.getRuntime().availableProcessors() - 1).coerceIn(2, 4)
    private val executor = ThreadPoolExecutor(
        threads, threads, 30L, TimeUnit.SECONDS, LinkedBlockingQueue(),
    ) { runnable ->
        Thread(runnable, "sgl-android-loader").apply { isDaemon = true }
    }.apply { allowCoreThreadTimeOut(true) }

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
