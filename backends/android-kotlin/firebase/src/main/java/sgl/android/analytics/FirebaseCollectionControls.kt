package sgl.android.analytics

/** Optional Android capability. Null fields leave the persisted Firebase choice unchanged. */
data class FirebaseConsent(
    val analyticsStorage: ConsentChoice? = null,
    val adStorage: ConsentChoice? = null,
    val adUserData: ConsentChoice? = null,
    val adPersonalization: ConsentChoice? = null,
)

enum class ConsentChoice { GRANTED, DENIED }

interface FirebaseCollectionControls {
    /** Firebase persists this independently of consent and ad personalization. */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    /** Partial updates do not reset the other consent dimensions. Firebase persists choices. */
    fun setConsent(consent: FirebaseConsent)
}

internal interface FirebaseControlClient {
    fun collection(enabled: Boolean)
    fun consent(choices: Map<String, ConsentChoice>)
}

internal class FirebaseControlDelegate(
    private val client: FirebaseControlClient,
    private val testLab: Boolean,
) : FirebaseCollectionControls {
    override fun setAnalyticsCollectionEnabled(enabled: Boolean) = client.collection(enabled && !testLab)
    override fun setConsent(consent: FirebaseConsent) {
        val choices = consent.choices()
        if (choices.isNotEmpty()) client.consent(choices)
    }
}

internal fun FirebaseConsent.choices(): Map<String, ConsentChoice> = buildMap {
    analyticsStorage?.let { put("ANALYTICS_STORAGE", it) }
    adStorage?.let { put("AD_STORAGE", it) }
    adUserData?.let { put("AD_USER_DATA", it) }
    adPersonalization?.let { put("AD_PERSONALIZATION", it) }
}
