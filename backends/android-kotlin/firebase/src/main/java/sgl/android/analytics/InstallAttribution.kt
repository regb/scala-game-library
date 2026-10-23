package sgl.android.analytics

import java.net.URLDecoder

/** No raw referrer or click/campaign ID values are retained. Times are Unix seconds from Play. */
data class InstallAttributionResult(
    val status: AttributionStatus,
    val source: String? = null,
    val medium: String? = null,
    val campaign: String? = null,
    val hasClickId: Boolean = false,
    val hasCampaignId: Boolean = false,
    val clickSeconds: Long = 0,
    val installSeconds: Long = 0,
    val serverClickSeconds: Long = 0,
    val serverInstallSeconds: Long = 0,
    val installVersion: String? = null,
    val retrievedSeconds: Long = 0,
) {
    val schemaVersion: Long get() = 1
    val provenance: String get() = "play_install_referrer"

    /** 14 bounded parameters, below Firebase's 25-parameter limit. */
    fun diagnosticFields(): Map<String, Any> = buildMap {
        put("schema_version", schemaVersion)
        put("provenance", provenance)
        put("status", status.name.lowercase())
        boundedAttributionText(source)?.let { put("referrer_source", it) }
        boundedAttributionText(medium)?.let { put("referrer_medium", it) }
        boundedAttributionText(campaign)?.let { put("referrer_campaign", it) }
        put("has_click_id", if (hasClickId) 1L else 0L)
        put("has_campaign_id", if (hasCampaignId) 1L else 0L)
        put("click_seconds", clickSeconds)
        put("install_seconds", installSeconds)
        put("server_click_seconds", serverClickSeconds)
        put("server_install_seconds", serverInstallSeconds)
        boundedAttributionText(installVersion)?.let { put("install_version", it) }
        put("retrieved_seconds", retrievedSeconds)
    }
}

internal fun emitInstallAttributionDiagnostic(
    result: InstallAttributionResult,
    allowed: () -> Boolean,
    log: (String, Map<String, Any>) -> Unit,
) {
    if (allowed()) log("install_attribution_diagnostic", result.diagnosticFields())
}

enum class AttributionStatus {
    SUCCESS, WITHHELD, UNSUPPORTED, UNAVAILABLE, DEVELOPER_ERROR, DISCONNECTED,
    TIMEOUT, MALFORMED, INTERNAL_ERROR, RETRIES_EXHAUSTED
}

internal data class NativeReferrer(
    val query: String,
    val click: Long = 0,
    val install: Long = 0,
    val serverClick: Long = 0,
    val serverInstall: Long = 0,
    val version: String? = null,
    val retrieved: Long = 0,
)

internal fun boundedAttributionText(value: String?): String? = value?.takeIf {
    it.length in 1..100 && it != "(not set)" && it.all { c -> c.isLetterOrDigit() || c in " _.,:-" }
}

internal fun parseReferrer(native: NativeReferrer): InstallAttributionResult {
    if (native.query.length > 8192) return InstallAttributionResult(AttributionStatus.MALFORMED)
    val values = mutableMapOf<String, String>()
    try {
        for (pair in native.query.split('&')) {
            val parts = pair.split('=', limit = 2)
            val key = URLDecoder.decode(parts[0], "UTF-8")
            if (key in setOf("utm_source", "utm_medium", "utm_campaign", "utm_id", "gad_campaignid", "gclid", "gbraid", "wbraid")) {
                // Ambiguous duplicate keys are not evidence of a campaign.
                if (values.containsKey(key)) return InstallAttributionResult(AttributionStatus.MALFORMED)
                values[key] = URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8")
            }
        }
    } catch (_: IllegalArgumentException) {
        return InstallAttributionResult(AttributionStatus.MALFORMED)
    }
    fun present(key: String) = values[key]?.let { it.isNotBlank() && it != "(not set)" } == true
    return InstallAttributionResult(
        AttributionStatus.SUCCESS,
        boundedAttributionText(values["utm_source"]),
        boundedAttributionText(values["utm_medium"]),
        boundedAttributionText(values["utm_campaign"]),
        listOf("gclid", "gbraid", "wbraid").any(::present),
        listOf("utm_id", "gad_campaignid").any(::present),
        native.click.coerceAtLeast(0), native.install.coerceAtLeast(0),
        native.serverClick.coerceAtLeast(0), native.serverInstall.coerceAtLeast(0),
        boundedAttributionText(native.version), native.retrieved.coerceAtLeast(0),
    )
}

internal interface ReferrerClient {
    /** Recheck mayRead on the native callback thread immediately before accessing the payload. */
    fun start(mayRead: () -> Boolean, callback: (AttributionStatus, NativeReferrer?) -> Unit)
    fun close()
}
internal interface ReferrerStore {
    var attempts: Int
    var result: InstallAttributionResult?
}
internal interface ReferrerScheduler {
    fun execute(action: () -> Unit)
    fun delay(milliseconds: Long, action: () -> Unit)
}

/** Capture state and results run on one serialized scheduler; the native read guard uses a published policy snapshot. */
internal class ReferrerCapture(
    private val client: () -> ReferrerClient,
    private val store: ReferrerStore,
    private val scheduler: ReferrerScheduler,
) {
    private data class Request(val allowed: () -> Boolean, val callback: (InstallAttributionResult) -> Unit)
    private val requests = mutableListOf<Request>()
    private var running = false
    @Volatile private var generation = 0
    // Immutable snapshots let the native callback evaluate current choices without racing the worker's request list.
    @Volatile private var readPolicies: List<() -> Boolean> = emptyList()

    fun retrieve(allowed: () -> Boolean, callback: (InstallAttributionResult) -> Unit) = scheduler.execute {
        if (!allowed()) callback(InstallAttributionResult(AttributionStatus.WITHHELD))
        else {
            requests += Request(allowed, callback)
            readPolicies = requests.map { it.allowed }
            if (!running) {
                running = true
                store.result?.let { finish(it) } ?: attempt()
            }
        }
    }

    private fun finish(result: InstallAttributionResult) {
        running = false
        readPolicies = emptyList()
        val pending = requests.toList()
        requests.clear()
        pending.forEach {
            it.callback(if (it.allowed()) result else InstallAttributionResult(AttributionStatus.WITHHELD))
        }
    }

    private fun attempt() {
        if (requests.none { it.allowed() }) return finish(InstallAttributionResult(AttributionStatus.WITHHELD))
        if (store.attempts >= 3) return finish(InstallAttributionResult(AttributionStatus.RETRIES_EXHAUSTED))
        store.attempts++
        val token = ++generation
        var connection: ReferrerClient? = null
        fun complete(status: AttributionStatus, native: NativeReferrer?) {
            if (token != generation) return
            ++generation
            try { connection?.close() } catch (_: Exception) { /* No native exception details are logged. */ }
            if (requests.none { it.allowed() }) return finish(InstallAttributionResult(AttributionStatus.WITHHELD))
            val result = if (status == AttributionStatus.SUCCESS && native != null) parseReferrer(native)
                else InstallAttributionResult(if (status == AttributionStatus.SUCCESS) AttributionStatus.INTERNAL_ERROR else status)
            if (result.status == AttributionStatus.SUCCESS) store.result = result
            if (result.status in setOf(AttributionStatus.UNAVAILABLE, AttributionStatus.DISCONNECTED, AttributionStatus.TIMEOUT, AttributionStatus.INTERNAL_ERROR) && store.attempts < 3) {
                scheduler.delay(1000L * store.attempts) { attempt() }
            } else finish(result)
        }
        scheduler.delay(10000) { complete(AttributionStatus.TIMEOUT, null) }
        try {
            connection = client()
            connection.start(
                mayRead = { readPolicies.any { it() } && token == generation },
                callback = { status, native -> scheduler.execute { complete(status, native) } },
            )
        } catch (_: Exception) {
            complete(AttributionStatus.INTERNAL_ERROR, null)
        }
    }
}
