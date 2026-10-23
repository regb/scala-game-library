package sgl.android.ads

import org.junit.Assert.*
import org.junit.Test
import sgl.ads.AdObserver
import sgl.ads.AdRevenue

class AdLifecycleTest {
    private class Observer : AdObserver() {
        val events = mutableListOf<String>()
        var revenue: AdRevenue? = null
        override fun onImpression(format: String, adUnitId: String) { events += "impression" }
        override fun onDismissed(format: String, adUnitId: String) { events += "dismissed" }
        override fun onShowFailed(format: String, adUnitId: String, errorCode: Int) { events += "error:$errorCode" }
        override fun onPaid(revenue: AdRevenue) { events += "paid"; this.revenue = revenue }
    }
    @Test fun noSyntheticImpressionAndDuplicateNativeCallbacksAreIgnored() {
        val observer = Observer()
        val lifecycle = AdLifecycle({ observer }, "interstitial", "unit")
        assertTrue(observer.events.isEmpty())
        lifecycle.impression()
        lifecycle.impression()
        lifecycle.paid(1234567, "USD", 1, "network")
        lifecycle.paid(1234567, "USD", 1, "network")
        assertTrue(lifecycle.close())
        assertFalse(lifecycle.close(3))
        lifecycle.impression()
        assertEquals(listOf("impression", "paid", "dismissed"), observer.events)
        assertEquals(1.234567, observer.revenue!!.currencyUnits(), 0.000000001)
        assertEquals(1234567L, observer.revenue!!.valueMicros())
        assertEquals("USD", observer.revenue!!.currencyCode())
        assertEquals(1, observer.revenue!!.precision())
        assertEquals("network", observer.revenue!!.network())
    }
    @Test fun terminalStateIsReleasedBeforeObserverCanRequestAnotherAd() {
        var showing = true
        val observer = object : AdObserver() {
            override fun onDismissed(format: String, adUnitId: String) { assertFalse(showing) }
        }
        val lifecycle = AdLifecycle({ observer }, "interstitial", "unit")
        lifecycle.close(beforeNotify = { showing = false })
        assertFalse(lifecycle.close(beforeNotify = { error("duplicate terminal cleanup") }))
    }

    @Test fun showFailureDoesNotImplyAnImpressionOrDismissal() {
        val observer = Observer()
        val lifecycle = AdLifecycle({ observer }, "rewarded", "unit")
        assertTrue(lifecycle.close(7))
        assertFalse(lifecycle.close())
        assertEquals(listOf("error:7"), observer.events)
    }
    @Test fun latePaidCallbackIsStillObservedOnceWithoutInventingAnImpression() {
        val observer = Observer()
        val lifecycle = AdLifecycle({ observer }, "rewarded", "unit")
        lifecycle.close()
        lifecycle.paid(1, "EUR", 3, "network")
        lifecycle.paid(1, "EUR", 3, "network")
        assertEquals(listOf("dismissed", "paid"), observer.events)
        assertEquals(0.000001, observer.revenue!!.currencyUnits(), 0.0000000001)
    }
}
