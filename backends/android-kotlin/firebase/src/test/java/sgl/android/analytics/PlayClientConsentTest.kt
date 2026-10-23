package sgl.android.analytics

import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import org.junit.Assert.*
import org.junit.Test
import java.util.PriorityQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** Exercises the production setup callback through the native getter boundary. */
class PlayClientConsentTest {
    private class Scheduler : ReferrerScheduler {
        private data class Task(val at: Long, val order: Int, val run: () -> Unit)
        private val tasks = PriorityQueue<Task>(compareBy<Task> { it.at }.thenBy { it.order })
        private var now = 0L
        private var order = 0
        var executing = false
        override fun execute(action: () -> Unit) { tasks += Task(now, order++, action) }
        override fun delay(milliseconds: Long, action: () -> Unit) { tasks += Task(now + milliseconds, order++, action) }
        fun runReady() {
            while (tasks.isNotEmpty() && tasks.peek()!!.at <= now) {
                executing = true
                try { tasks.remove().run() } finally { executing = false }
            }
        }
        fun advance(milliseconds: Long) { now += milliseconds; runReady() }
    }
    private class Store : ReferrerStore {
        override var attempts = 0
        override var result: InstallAttributionResult? = null
    }
    private class NativeConnection : PlayReferrerConnection {
        lateinit var listener: InstallReferrerStateListener
        var reads = 0
        var closes = 0
        var onRead: () -> Unit = {}
        override fun start(listener: InstallReferrerStateListener) { this.listener = listener }
        override fun read(): NativeReferrer {
            reads++
            onRead()
            return NativeReferrer("utm_source=google&gclid=private-click-id", 10, 20)
        }
        override fun close() { closes++ }
        fun setup(response: Int = InstallReferrerResponse.OK) = onNativeThread {
            listener.onInstallReferrerSetupFinished(response)
        }
    }
    private class Capture {
        val scheduler = Scheduler()
        val store = Store()
        val connections = mutableListOf<NativeConnection>()
        val capture = ReferrerCapture({
            val native = NativeConnection()
            connections += native
            PlayClient(native)
        }, store, scheduler)
    }

    @Test fun revokingWhileConnectingPreventsNativeGetterAccess() {
        val f = Capture()
        val allowed = AtomicBoolean(true)
        var result: InstallAttributionResult? = null
        f.capture.retrieve({ allowed.get() }) {
            assertTrue(f.scheduler.executing)
            result = it
        }
        f.scheduler.runReady()
        assertEquals(1, f.store.attempts)
        val native = f.connections.single()
        allowed.set(false)
        native.setup()
        assertEquals(0, native.reads)
        assertNull(result) // Native callbacks still dispatch result handling to the serialized worker.
        f.scheduler.runReady()
        assertEquals(AttributionStatus.WITHHELD, result?.status)
        assertFalse(result!!.hasClickId)
        assertNull(f.store.result)
        assertEquals(1, native.closes)
        f.scheduler.advance(10000)
        assertEquals(1, f.store.attempts)
        assertEquals(1, native.closes)
    }

    @Test fun laterCoalescedRequestCanAuthorizeReadWhileRevokedRequestIsWithheld() {
        val f = Capture()
        val firstAllowed = AtomicBoolean(true)
        val secondAllowed = AtomicBoolean(true)
        var first: InstallAttributionResult? = null
        var second: InstallAttributionResult? = null
        f.capture.retrieve({ firstAllowed.get() }) { first = it }
        f.scheduler.runReady() // Connect before registering the second request.
        f.capture.retrieve({ secondAllowed.get() }) { second = it }
        f.scheduler.runReady()
        firstAllowed.set(false)
        val native = f.connections.single()
        native.setup()
        f.scheduler.runReady()
        assertEquals(1, native.reads)
        assertEquals(1, native.closes)
        assertEquals(AttributionStatus.WITHHELD, first?.status)
        assertNull(first?.source)
        assertEquals(AttributionStatus.SUCCESS, second?.status)
        assertEquals("google", second?.source)
        assertTrue(second!!.hasClickId)
        assertEquals(second, f.store.result)
        assertFalse(f.store.result.toString().contains("private-click-id"))
        f.capture.retrieve({ true }) { assertEquals(second, it) }
        f.scheduler.runReady()
        assertEquals(1, f.connections.size)
        assertEquals(1, native.reads)
    }

    @Test fun revokingEveryCoalescedRequestPreventsTheGetter() {
        val f = Capture()
        val allowed = List(2) { AtomicBoolean(true) }
        val results = mutableListOf<AttributionStatus>()
        allowed.forEach { policy -> f.capture.retrieve({ policy.get() }) { results += it.status } }
        f.scheduler.runReady()
        allowed.forEach { it.set(false) }
        f.connections.single().setup()
        f.scheduler.runReady()
        assertEquals(listOf(AttributionStatus.WITHHELD, AttributionStatus.WITHHELD), results)
        assertEquals(0, f.connections.single().reads)
        assertEquals(1, f.connections.single().closes)
        assertNull(f.store.result)
    }

    @Test fun timedOutSetupCannotReadUsingTheNextAttemptsPermission() {
        val f = Capture()
        var result: InstallAttributionResult? = null
        f.capture.retrieve({ true }) { result = it }
        f.scheduler.runReady()
        val expired = f.connections.single()
        f.scheduler.advance(10000)
        assertEquals(1, expired.closes)
        f.scheduler.advance(1000)
        val retry = f.connections.last()
        assertEquals(2, f.connections.size)
        expired.setup()
        f.scheduler.runReady()
        assertEquals(0, expired.reads)
        assertNull(result)
        retry.setup()
        f.scheduler.runReady()
        assertEquals(AttributionStatus.SUCCESS, result?.status)
        assertEquals(1, retry.reads)
        assertEquals(1, retry.closes)
        f.scheduler.advance(10000)
        assertEquals(2, f.store.attempts)
    }

    @Test fun blockedNativeGetterDoesNotBlockWorkerTimeoutOrAllowLateDelivery() {
        val f = Capture()
        val allowed = AtomicBoolean(true)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val results = mutableListOf<AttributionStatus>()
        f.capture.retrieve({ allowed.get() }) { results += it.status }
        f.scheduler.runReady()
        val native = f.connections.single()
        native.onRead = {
            entered.countDown()
            check(release.await(5, TimeUnit.SECONDS))
        }
        val setup = FutureTask<Unit> { native.listener.onInstallReferrerSetupFinished(InstallReferrerResponse.OK) }
        Thread(setup).start()
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            f.scheduler.advance(10000)
            assertEquals(1, native.closes)
            allowed.set(false)
            f.scheduler.advance(1000)
            assertEquals(listOf(AttributionStatus.WITHHELD), results)
        } finally {
            release.countDown()
            setup.get(5, TimeUnit.SECONDS)
        }
        f.scheduler.runReady()
        assertEquals(1, native.reads)
        assertEquals(1, native.closes)
        assertEquals(listOf(AttributionStatus.WITHHELD), results)
        assertNull(f.store.result)
    }

    @Test fun getterExceptionsKeepRetryBudgetAndConnectionCleanup() {
        val f = Capture()
        var result: InstallAttributionResult? = null
        f.capture.retrieve({ true }) { result = it }
        f.scheduler.runReady()
        for (attempt in 1..3) {
            val native = f.connections.last()
            native.onRead = { error("native failure with private details") }
            native.setup()
            f.scheduler.runReady()
            assertEquals(1, native.reads)
            assertEquals(1, native.closes)
            if (attempt < 3) f.scheduler.advance(1000L * attempt)
        }
        assertEquals(3, f.store.attempts)
        assertEquals(AttributionStatus.INTERNAL_ERROR, result?.status)
        assertNull(f.store.result)
        f.capture.retrieve({ true }) { result = it }
        f.scheduler.runReady()
        assertEquals(AttributionStatus.RETRIES_EXHAUSTED, result?.status)
        assertEquals(3, f.connections.size)
    }

    @Test fun nativeErrorResponsesNeverAccessPayload() {
        for ((response, expected) in listOf(
            InstallReferrerResponse.FEATURE_NOT_SUPPORTED to AttributionStatus.UNSUPPORTED,
            InstallReferrerResponse.DEVELOPER_ERROR to AttributionStatus.DEVELOPER_ERROR,
        )) {
            val f = Capture()
            var status: AttributionStatus? = null
            f.capture.retrieve({ true }) { status = it.status }
            f.scheduler.runReady()
            val native = f.connections.single()
            native.setup(response)
            f.scheduler.runReady()
            assertEquals(expected, status)
            assertEquals(0, native.reads)
            assertEquals(1, native.closes)
        }
    }

    @Test fun revokingDuringGetterStillWithholdsCacheAndDelivery() {
        val f = Capture()
        val allowed = AtomicBoolean(true)
        var status: AttributionStatus? = null
        f.capture.retrieve({ allowed.get() }) { status = it.status }
        f.scheduler.runReady()
        val native = f.connections.single()
        native.onRead = { allowed.set(false) }
        native.setup()
        f.scheduler.runReady()
        assertEquals(1, native.reads)
        assertEquals(AttributionStatus.WITHHELD, status)
        assertNull(f.store.result)
        assertEquals(1, native.closes)
    }

    @Test fun duplicateSetupCallbacksOnlyAccessTheGetterOnce() {
        val f = Capture()
        var deliveries = 0
        f.capture.retrieve({ true }) { deliveries++ }
        f.scheduler.runReady()
        val native = f.connections.single()
        native.setup()
        native.setup() // Before the worker has processed the first completion.
        onNativeThread { native.listener.onInstallReferrerServiceDisconnected() }
        f.scheduler.runReady()
        assertEquals(1, native.reads)
        assertEquals(1, deliveries)
        assertEquals(1, native.closes)
    }

    companion object {
        private fun onNativeThread(action: () -> Unit) {
            val failure = AtomicReference<Throwable?>()
            val thread = Thread {
                try { action() } catch (error: Throwable) { failure.set(error) }
            }
            thread.start()
            thread.join()
            failure.get()?.let { throw it }
        }
    }
}
