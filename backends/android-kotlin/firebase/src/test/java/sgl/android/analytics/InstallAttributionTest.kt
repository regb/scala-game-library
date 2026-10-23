package sgl.android.analytics

import org.junit.Assert.*
import org.junit.Test
import java.util.PriorityQueue

class InstallAttributionTest {
    private class Scheduler : ReferrerScheduler {
        data class Task(val at: Long, val order: Int, val run: () -> Unit)
        private val tasks = PriorityQueue<Task>(compareBy<Task> { it.at }.thenBy { it.order })
        var now = 0L
        var order = 0
        var executing = false
        override fun execute(action: () -> Unit) { tasks += Task(now, order++, action) }
        override fun delay(milliseconds: Long, action: () -> Unit) { tasks += Task(now + milliseconds, order++, action) }
        fun drain() {
            while (tasks.isNotEmpty()) {
                val task = tasks.remove()
                now = task.at
                executing = true
                task.run()
                executing = false
            }
        }
    }
    private class Store : ReferrerStore {
        override var attempts = 0
        override var result: InstallAttributionResult? = null
    }
    private class Client : ReferrerClient {
        lateinit var callback: (AttributionStatus, NativeReferrer?) -> Unit
        var closes = 0
        override fun start(mayRead: () -> Boolean, callback: (AttributionStatus, NativeReferrer?) -> Unit) { this.callback = callback }
        override fun close() { closes++ }
    }

    @Test fun parsesOnlyAllowlistedFieldsAndNeverRetainsIds() {
        val r = parseReferrer(NativeReferrer("utm_source=google&utm_medium=cpc&utm_campaign=Summer+sale&gclid=SECRET&gad_campaignid=123&email=private%40example.com", 10, 20, 11, 21, "1.2"))
        assertEquals(AttributionStatus.SUCCESS, r.status)
        assertEquals("Summer sale", r.campaign)
        assertTrue(r.hasClickId)
        assertTrue(r.hasCampaignId)
        assertEquals(11, r.serverClickSeconds)
        assertEquals(21, r.serverInstallSeconds)
        assertFalse(r.toString().contains("SECRET"))
        assertFalse(r.diagnosticFields().toString().contains("private"))
        assertTrue(r.diagnosticFields().size <= 25)
        assertFalse(r.diagnosticFields().keys.any { it.startsWith("firebase_") })
    }

    @Test fun diagnosticsRecheckPolicyAndNeverReplayReservedAttributionEvents() {
        val result = parseReferrer(NativeReferrer("gclid=secret&gad_campaignid=123&utm_campaign=campaign"))
        val names = mutableListOf<String>()
        var allowed = false
        val log: (String, Map<String, Any>) -> Unit = { name, fields ->
            names += name
            assertTrue(fields.size <= 25)
            assertFalse(fields.toString().contains("secret"))
            assertFalse(fields.containsKey("gad_campaignid"))
        }
        emitInstallAttributionDiagnostic(result, { allowed }, log)
        assertTrue(names.isEmpty())
        allowed = true
        emitInstallAttributionDiagnostic(result, { allowed }, log)
        allowed = false
        emitInstallAttributionDiagnostic(result, { allowed }, log)
        assertEquals(listOf("install_attribution_diagnostic"), names)
    }

    @Test fun successfulRetryStopsFurtherConnections() {
        val scheduler = Scheduler()
        val store = Store()
        val clients = mutableListOf<Client>()
        var result: InstallAttributionResult? = null
        val capture = ReferrerCapture({ Client().also {
            clients += it
            scheduler.execute {
                if (clients.size == 1) it.callback(AttributionStatus.DISCONNECTED, null)
                else it.callback(AttributionStatus.SUCCESS, NativeReferrer("utm_medium=cpc"))
            }
        } }, store, scheduler)
        capture.retrieve({ true }) { result = it }
        scheduler.drain()
        assertEquals(2, clients.size)
        assertTrue(clients.all { it.closes == 1 })
        assertEquals("cpc", result?.medium)
        assertEquals(result, store.result)
    }

    @Test fun absentMalformedEncodedAndBoundedFields() {
        assertEquals(AttributionStatus.SUCCESS, parseReferrer(NativeReferrer("")).status)
        assertFalse(parseReferrer(NativeReferrer("gclid=&utm_id=(not+set)")).hasCampaignId)
        assertEquals("café", parseReferrer(NativeReferrer("utm_campaign=caf%C3%A9")).campaign)
        for (query in listOf("utm_source=%ZZ", "utm_source=a&utm_source=b", "x".repeat(8193))) {
            assertEquals(AttributionStatus.MALFORMED, parseReferrer(NativeReferrer(query)).status)
        }
        assertNull(parseReferrer(NativeReferrer("utm_source=https%3A%2F%2Fsecret&ignored=%ZZ")).source)
        assertNull(parseReferrer(NativeReferrer("utm_campaign=" + "x".repeat(101))).campaign)
        assertEquals(0L, parseReferrer(NativeReferrer("", -1)).clickSeconds)
    }

    @Test fun successfulCaptureIsCachedAndCoalescesRecreation() {
        val scheduler = Scheduler()
        val store = Store()
        val client = Client()
        var creates = 0
        val capture = ReferrerCapture({ creates++; client }, store, scheduler)
        val results = mutableListOf<InstallAttributionResult>()
        val callback: (InstallAttributionResult) -> Unit = { assertTrue(scheduler.executing); results += it }
        capture.retrieve({ true }, callback)
        capture.retrieve({ true }, callback)
        scheduler.execute { client.callback(AttributionStatus.SUCCESS, NativeReferrer("utm_source=google")) }
        scheduler.drain()
        assertEquals(2, results.size)
        assertEquals(1, client.closes)
        ReferrerCapture({ error("must use persisted cache") }, store, scheduler).retrieve({ true }, callback)
        scheduler.drain()
        assertEquals(3, results.size)
        assertEquals(1, creates)
        assertEquals(1, store.attempts)
        // Late native events cannot complete again or evict the success cache.
        client.callback(AttributionStatus.DISCONNECTED, null)
        scheduler.drain()
        assertEquals(3, results.size)
        assertEquals(AttributionStatus.SUCCESS, store.result?.status)
    }

    @Test fun transientFailuresRetryAtMostThreeTimesAndCloseEveryConnection() {
        val scheduler = Scheduler()
        val store = Store()
        val clients = mutableListOf<Client>()
        var delivered: AttributionStatus? = null
        val capture = ReferrerCapture({ Client().also {
            clients += it
            scheduler.execute { it.callback(AttributionStatus.UNAVAILABLE, null) }
        } }, store, scheduler)
        capture.retrieve({ true }) { delivered = it.status }
        scheduler.drain()
        assertEquals(3, clients.size)
        assertTrue(clients.all { it.closes == 1 })
        assertEquals(AttributionStatus.UNAVAILABLE, delivered)
        capture.retrieve({ true }) { delivered = it.status }
        scheduler.drain()
        assertEquals(AttributionStatus.RETRIES_EXHAUSTED, delivered)
        assertEquals(3, clients.size)
    }

    @Test fun timeoutsAndThrownStartAreBoundedAndCleanedUp() {
        for (throws in listOf(false, true)) {
            val scheduler = Scheduler()
            var closes = 0
            var calls = 0
            val capture = ReferrerCapture({ object : ReferrerClient {
                override fun start(mayRead: () -> Boolean, callback: (AttributionStatus, NativeReferrer?) -> Unit) {
                    calls++
                    if (throws) error("native error containing sensitive details")
                }
                override fun close() { closes++ }
            } }, Store(), scheduler)
            var status: AttributionStatus? = null
            capture.retrieve({ true }) { status = it.status }
            scheduler.drain()
            assertEquals(3, calls)
            assertEquals(3, closes)
            assertEquals(if (throws) AttributionStatus.INTERNAL_ERROR else AttributionStatus.TIMEOUT, status)
        }
    }

    @Test fun unsupportedAndDeveloperErrorsDoNotRetryInOneRequest() {
        for (status in listOf(AttributionStatus.UNSUPPORTED, AttributionStatus.DEVELOPER_ERROR)) {
            val scheduler = Scheduler()
            val client = Client()
            var delivered: AttributionStatus? = null
            val store = Store()
            val capture = ReferrerCapture({ client }, store, scheduler)
            capture.retrieve({ true }) { delivered = it.status }
            scheduler.execute { client.callback(status, null) }
            scheduler.drain()
            assertEquals(status, delivered)
            assertEquals(1, store.attempts)
            assertEquals(1, client.closes)
        }
    }

    @Test fun denialBeforeStartOrDuringNativeReadWithholdsDataAndCache() {
        val scheduler = Scheduler()
        val store = Store()
        val client = Client()
        val capture = ReferrerCapture({ client }, store, scheduler)
        var allowed = false
        var delivered: InstallAttributionResult? = null
        capture.retrieve({ allowed }) { delivered = it }
        scheduler.drain()
        assertEquals(0, store.attempts)
        assertEquals(AttributionStatus.WITHHELD, delivered?.status)
        allowed = true
        capture.retrieve({ allowed }) { delivered = it }
        scheduler.execute {
            allowed = false
            client.callback(AttributionStatus.SUCCESS, NativeReferrer("gclid=secret"))
        }
        scheduler.drain()
        assertEquals(AttributionStatus.WITHHELD, delivered?.status)
        assertNull(store.result)
        assertEquals(1, client.closes)
    }

    @Test fun revocationDuringRetryDoesNotStartAnotherNativeRead() {
        val scheduler = Scheduler()
        val store = Store()
        val client = Client()
        var allowed = true
        var delivered: AttributionStatus? = null
        val capture = ReferrerCapture({ client }, store, scheduler)
        capture.retrieve({ allowed }) { delivered = it.status }
        scheduler.execute { client.callback(AttributionStatus.DISCONNECTED, null) }
        scheduler.delay(500) { allowed = false }
        scheduler.drain()
        assertEquals(1, store.attempts)
        assertEquals(AttributionStatus.WITHHELD, delivered)
    }
}
