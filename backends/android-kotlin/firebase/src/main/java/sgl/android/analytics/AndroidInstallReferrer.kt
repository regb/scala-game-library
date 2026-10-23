package sgl.android.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AtomicFile
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Process-wide, opt-in capture, independent of Firebase's own automatic attribution.
 * No work starts until retrieve. Native work and cache IO run off the UI thread.
 */
class AndroidInstallReferrer private constructor(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadScheduledExecutor { task ->
        Thread(task, "sgl-install-referrer").apply { isDaemon = true }
    }
    private val scheduler = object : ReferrerScheduler {
        override fun execute(action: () -> Unit) { worker.execute(action) }
        override fun delay(milliseconds: Long, action: () -> Unit) {
            worker.schedule(action, milliseconds, TimeUnit.MILLISECONDS)
        }
    }
    private val capture by lazy {
        val installed = app.packageManager.getPackageInfo(app.packageName, 0).firstInstallTime
        ReferrerCapture({ PlayClient(app) }, FileReferrerStore(File(app.noBackupFilesDir, "sgl-install-attribution.json"), installed), scheduler)
    }

    /**
     * Policy must be thread-safe and checked against current collection AND consent choices.
     * It is rechecked before retrieval, the native payload getter, retries, cache delivery and UI delivery.
     * The native getter proceeds only if at least one coalesced request is still allowed.
     * Callback always runs on the main thread, including withheld/error/cached results.
     * Do not capture an Activity in policy; callbacks should tolerate a destroyed screen.
     */
    fun retrieve(allowed: () -> Boolean, callback: (InstallAttributionResult) -> Unit) {
        scheduler.execute {
            capture.retrieve(allowed) { result ->
                deliverAttributionResult(main, result, allowed, callback)
            }
        }
    }

    companion object {
        // Only the application context is retained, never the caller's Activity.
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: AndroidInstallReferrer? = null
        fun getInstance(context: Context): AndroidInstallReferrer = instance ?: synchronized(this) {
            instance ?: AndroidInstallReferrer(context.applicationContext).also { instance = it }
        }
    }
}

internal fun deliverAttributionResult(
    main: Handler,
    result: InstallAttributionResult,
    allowed: () -> Boolean,
    callback: (InstallAttributionResult) -> Unit,
) {
    main.post {
        callback(if (allowed()) result else InstallAttributionResult(AttributionStatus.WITHHELD))
    }
}

/** Injectable boundary around the real native payload getter, not an already-read result. */
internal interface PlayReferrerConnection {
    fun start(listener: InstallReferrerStateListener)
    fun read(): NativeReferrer
    fun close()
}

internal class PlayClient(private val connection: PlayReferrerConnection) : ReferrerClient {
    constructor(context: Context) : this(AndroidPlayReferrerConnection(context))

    override fun start(mayRead: () -> Boolean, callback: (AttributionStatus, NativeReferrer?) -> Unit) {
        val finished = AtomicBoolean(false)
        connection.start(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (!finished.compareAndSet(false, true)) return
                val status = when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> AttributionStatus.SUCCESS
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> AttributionStatus.UNSUPPORTED
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> AttributionStatus.UNAVAILABLE
                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> AttributionStatus.DEVELOPER_ERROR
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> AttributionStatus.DISCONNECTED
                    else -> AttributionStatus.INTERNAL_ERROR
                }
                try {
                    if (status != AttributionStatus.SUCCESS) callback(status, null)
                    else if (!mayRead()) callback(AttributionStatus.WITHHELD, null)
                    else callback(AttributionStatus.SUCCESS, connection.read())
                } catch (_: Exception) {
                    callback(AttributionStatus.INTERNAL_ERROR, null)
                }
            }
            override fun onInstallReferrerServiceDisconnected() {
                if (finished.compareAndSet(false, true)) callback(AttributionStatus.DISCONNECTED, null)
            }
        })
    }
    override fun close() = connection.close()
}

private class AndroidPlayReferrerConnection(context: Context) : PlayReferrerConnection {
    private val client = InstallReferrerClient.newBuilder(context.applicationContext).build()
    override fun start(listener: InstallReferrerStateListener) = client.startConnection(listener)
    override fun read(): NativeReferrer {
        val details = client.installReferrer
        return NativeReferrer(details.installReferrer, details.referrerClickTimestampSeconds,
            details.installBeginTimestampSeconds, details.referrerClickTimestampServerSeconds,
            details.installBeginTimestampServerSeconds, details.installVersion, System.currentTimeMillis() / 1000)
    }
    override fun close() = client.endConnection()
}

/** No-backup storage plus firstInstallTime prevents restored preferences from impersonating this install. */
internal class FileReferrerStore(file: File, private val installed: Long) : ReferrerStore {
    private val atomic = AtomicFile(file)
    private var data = try {
        JSONObject(atomic.openRead().bufferedReader().use { it.readText() }).takeIf {
            it.optLong("installation") == installed && it.optInt("schema") == 1
        } ?: JSONObject()
    } catch (_: Exception) { JSONObject() }

    override var attempts: Int
        get() = data.optInt("attempts").coerceIn(0, 3)
        set(value) { data.put("attempts", value); save() }
    override var result: InstallAttributionResult?
        get() {
            val r = data.optJSONObject("result") ?: return null
            fun text(key: String) = boundedAttributionText(r.optString(key).takeIf { it.isNotEmpty() })
            return InstallAttributionResult(AttributionStatus.SUCCESS,
                text("referrer_source"), text("referrer_medium"), text("referrer_campaign"),
                r.optLong("has_click_id") == 1L, r.optLong("has_campaign_id") == 1L,
                r.optLong("click_seconds"), r.optLong("install_seconds"),
                r.optLong("server_click_seconds"), r.optLong("server_install_seconds"), text("install_version"),
                r.optLong("retrieved_seconds"))
        }
        set(value) {
            if (value == null) data.remove("result")
            else data.put("result", JSONObject(value.diagnosticFields()))
            save()
        }
    private fun save() {
        data.put("installation", installed).put("schema", 1)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = atomic.startWrite()
            stream.write(data.toString().toByteArray(Charsets.UTF_8))
            atomic.finishWrite(stream)
        } catch (_: Exception) {
            atomic.failWrite(stream)
        }
    }
}
