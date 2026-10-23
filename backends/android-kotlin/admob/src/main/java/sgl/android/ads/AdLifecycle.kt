package sgl.android.ads

import sgl.ads.AdObserver
import sgl.ads.AdRevenue

/** One native ad instance. Does not emit analytics events or synthesize impressions. */
internal class AdLifecycle(
    private val observer: () -> AdObserver?,
    private val format: String,
    private val unit: String,
) {
    private var impressed = false
    private var paid = false
    private var closed = false
    fun impression() {
        if (!closed && !impressed) {
            impressed = true
            observer()?.onImpression(format, unit)
        }
    }
    fun paid(micros: Long, currency: String, precision: Int, network: String) {
        if (!paid) {
            paid = true
            observer()?.onPaid(AdRevenue(micros, currency, precision, network, format, unit))
        }
    }
    fun close(error: Int? = null, beforeNotify: () -> Unit = {}): Boolean {
        if (closed) return false
        closed = true
        beforeNotify()
        if (error == null) observer()?.onDismissed(format, unit)
        else observer()?.onShowFailed(format, unit, error)
        return true
    }
}
