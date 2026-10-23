package sgl.android.analytics

import android.os.Handler
import android.os.Looper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
@LooperMode(LooperMode.Mode.PAUSED)
class AttributionDeliveryTest {
    @Test fun nativeWorkerResultsAlwaysDispatchToMainAndRecheckRevocation() {
        for (revoke in listOf(false, true)) {
            val allowed = AtomicBoolean(true)
            var delivered: InstallAttributionResult? = null
            val nativeWorker = Thread {
                deliverAttributionResult(Handler(Looper.getMainLooper()),
                    parseReferrer(NativeReferrer("utm_source=google")), { allowed.get() }) {
                    assertEquals(Looper.getMainLooper(), Looper.myLooper())
                    delivered = it
                }
            }
            nativeWorker.start()
            nativeWorker.join()
            assertNull(delivered)
            if (revoke) allowed.set(false)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(if (revoke) AttributionStatus.WITHHELD else AttributionStatus.SUCCESS, delivered?.status)
            assertEquals(if (revoke) null else "google", delivered?.source)
        }
    }
}
