package sgl.android

import sgl.proxy.SchedulerProxy
import sgl.util.ChunkedTask
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

class AndroidSchedulerProxy: SchedulerProxy {
    private val tasks = ConcurrentLinkedQueue<ChunkedTask>()
    private val lock = Any()
    private var executor: ExecutorService? = null
    private var workers: List<Worker> = emptyList()

    override fun schedule(task: ChunkedTask?) {
        if (task != null) {
            tasks.add(task)
        }
    }

    fun resume() {
        synchronized(lock) {
            if (executor != null) return

            val newExecutor = Executors.newFixedThreadPool(4) { runnable ->
                Thread(runnable, "sgl-android-scheduler").apply { isDaemon = true }
            }
            val newWorkers = List(4) { Worker() }
            executor = newExecutor
            workers = newWorkers
            newWorkers.forEach { newExecutor.submit(it) }
        }
    }

    fun pause() {
        synchronized(lock) {
            workers.forEach { it.stop() }
            executor?.shutdownNow()
            workers = emptyList()
            executor = null
        }
    }

    fun shutdown() {
        pause()
        tasks.clear()
    }

    private inner class Worker: Runnable {
        private val shouldStop = AtomicBoolean(false)

        fun stop() {
            shouldStop.set(true)
        }

        override fun run() {
            while (!shouldStop.get()) {
                val task = tasks.poll()
                if (task == null) {
                    try {
                        Thread.sleep(50)
                    } catch (_: InterruptedException) {
                        shouldStop.set(true)
                    }
                    continue
                }

                try {
                    task.doRun(5L)
                    if (task.status().toString() != "Completed") {
                        tasks.add(task)
                    }
                } catch (throwable: Throwable) {
                    val sw = StringWriter()
                    throwable.printStackTrace(PrintWriter(sw, true))
                    println("Unexpected error while executing task ${task.name()}: ${throwable.message}\n$sw")
                }
            }
        }
    }
}
