package sgl.android.analytics

import org.junit.Assert.*
import org.junit.Test

class FirebaseCollectionControlsTest {
    /** Models the native client's persisted settings, shared by recreated delegates. */
    private class Native : FirebaseControlClient {
        var enabled = true
        val persisted = mutableMapOf<String, ConsentChoice>()
        override fun collection(enabled: Boolean) { this.enabled = enabled }
        override fun consent(choices: Map<String, ConsentChoice>) { persisted.putAll(choices) }
    }
    @Test fun allFourDimensionsUpdateIndependentlyAndSurviveDelegateRecreation() {
        val native = Native()
        val controls = FirebaseControlDelegate(native, false)
        controls.setConsent(FirebaseConsent(ConsentChoice.DENIED, ConsentChoice.DENIED, ConsentChoice.DENIED, ConsentChoice.DENIED))
        val updates = listOf(
            "ANALYTICS_STORAGE" to FirebaseConsent(analyticsStorage = ConsentChoice.GRANTED),
            "AD_STORAGE" to FirebaseConsent(adStorage = ConsentChoice.GRANTED),
            "AD_USER_DATA" to FirebaseConsent(adUserData = ConsentChoice.GRANTED),
            "AD_PERSONALIZATION" to FirebaseConsent(adPersonalization = ConsentChoice.GRANTED),
        )
        for ((key, update) in updates) {
            FirebaseControlDelegate(native, false).setConsent(update)
            assertEquals(ConsentChoice.GRANTED, native.persisted[key])
            assertEquals(4, native.persisted.size)
        }
        controls.setConsent(FirebaseConsent(adPersonalization = ConsentChoice.DENIED))
        assertTrue(native.enabled)
        assertEquals(ConsentChoice.GRANTED, native.persisted["ANALYTICS_STORAGE"])
        controls.setAnalyticsCollectionEnabled(false)
        assertFalse(native.enabled)
        assertEquals(ConsentChoice.DENIED, native.persisted["AD_PERSONALIZATION"])
        val before = native.persisted.toMap()
        controls.setConsent(FirebaseConsent())
        assertEquals(before, native.persisted)
    }
    @Test fun testLabCannotBeReenabledThroughPublicControl() {
        val native = Native()
        FirebaseControlDelegate(native, true).setAnalyticsCollectionEnabled(true)
        assertFalse(native.enabled)
    }
}
