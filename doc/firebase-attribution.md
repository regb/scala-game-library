# Firebase, consent and attribution on Android

SGL's optional Kotlin Firebase backend now uses Firebase BoM 35.1.0 and Analytics 24.0.0. The existing `AndroidFirebaseAnalytics(context)` constructor and `AbstractAnalytics` API are unchanged. Non-Android backends are unchanged. Gameplay milestones already work through `logCustomEvent`; choose their definitions in the game.

## Migration

The [official release notes](https://firebase.google.com/support/release-notes/android) and Google's Install Referrer Maven metadata were checked on October 23, 2026. Analytics 24 requires API 24. SGL still requires API 26 and targets API 37. BoM 34 removed the separate Firebase KTX modules; SGL uses the main `firebase-analytics` artifact, not `firebase-analytics-ktx`.

Analytics 24 ships Kotlin 2.4 metadata. AGP 9.2.1's default Kotlin compiler cannot read it. Both SGL's root Gradle build and generated projects now raise the built-in Kotlin plugin dependency to 2.4.21. Custom Gradle consumers must make the same change, without applying `org.jetbrains.kotlin.android` alongside AGP's built-in Kotlin:

```kotlin
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}
```

Keep Firebase libraries under the same BoM. SGL's optional Crashlytics alias remains BoM-managed. AdMob remains on 24.5.0. The Firebase library declares the normal INTERNET, ACCESS_NETWORK_STATE and WAKE_LOCK permissions required by Analytics. This change does not add identifier permissions as an attribution workaround. Review the final merged manifest and your app's data safety declarations, including permissions contributed by Google SDKs.

An SDK upgrade is not evidence that missing campaign IDs will be fixed.

## Defaults before automatic initialization

Firebase's content provider initializes before Activity wiring. Runtime setters alone cannot prevent collection before a consent screen. `sgl_android_app` accepts these validated string attributes:

| Attribute | Manifest metadata |
| --- | --- |
| `firebase_analytics_collection_enabled` | `firebase_analytics_collection_enabled` |
| `firebase_analytics_storage` | `google_analytics_default_allow_analytics_storage` |
| `firebase_ad_storage` | `google_analytics_default_allow_ad_storage` |
| `firebase_ad_user_data` | `google_analytics_default_allow_ad_user_data` |
| `firebase_ad_personalization` | `google_analytics_default_allow_ad_personalization_signals` |

Each accepts `""`, `"true"`, or `"false"`. Empty means omit the metadata and preserve Firebase's existing default, not deny. Supplying a default requires `google_services_json`. For an app that waits for a choice:

```python
sgl_android_app(
    # Existing game arguments omitted.
    google_services_json = "android/google-services.json",
    firebase_analytics_collection_enabled = "false",
    firebase_analytics_storage = "false",
    firebase_ad_storage = "false",
    firebase_ad_user_data = "false",
    firebase_ad_personalization = "false",
)
```

Custom Gradle apps can put the corresponding boolean metadata in their own application manifest. Firebase's permanently deactivated collection metadata, if used by a custom app, cannot be reversed by the runtime setter.

## Runtime choices

`AndroidFirebaseAnalytics` implements the optional Android-only `FirebaseCollectionControls` capability. There is no silent cross-platform consent setter in `AbstractAnalytics`.

```kotlin
val analytics = AndroidFirebaseAnalytics(context)
analytics.setConsent(FirebaseConsent(
    analyticsStorage = ConsentChoice.DENIED,
    adStorage = ConsentChoice.DENIED,
    adUserData = ConsentChoice.DENIED,
    adPersonalization = ConsentChoice.DENIED,
))
analytics.setAnalyticsCollectionEnabled(false)

// Later, update only a dimension for which the user has changed their choice.
analytics.setConsent(FirebaseConsent(adPersonalization = ConsentChoice.DENIED))
```

Import these types from `sgl.android.analytics`. A null consent field leaves that dimension unchanged. Firebase persists the supplied choices and collection override across process restarts. SGL does not reset them on Activity recreation. Firebase's persisted runtime settings take precedence over manifest defaults. Collection enabled/disabled is independent of the four consent dimensions. Denying personalized advertising does not mean analytics collection is disabled, and enabling collection must not be treated as granting consent. Test Lab collection cannot be re-enabled through SGL's setter.

Maintain the application's durable, thread-safe consent state as the source for its own policy decisions. Firebase does not expose a getter for these choices. Never grant consent, reset analytics data, retrieve extra identifiers or change a denied choice to improve attribution. Apply the user's actual choices and applicable policy. The [collection controls](https://firebase.google.com/docs/analytics/android/configure-data-collection) and [consent v2 guide](https://developers.google.com/tag-platform/security/guides/app-consent?platform=android) describe the native semantics.

## Opt-in Play referrer diagnostics

Firebase already reads Play's referrer automatically. SGL's separate capability is for diagnostics, not a replacement for Firebase attribution. The optional Firebase module includes the official Install Referrer 2.2 library. Merely linking it or constructing `AndroidFirebaseAnalytics` does not start SGL's referrer retrieval.

```kotlin
val referrer = AndroidInstallReferrer.getInstance(context)
referrer.retrieve(
    allowed = { consentStore.mayReadInstallReferrerDiagnostics() },
) { result ->
    // Main thread. Do not assume the original screen is still alive.
    // Optional, separate event. This policy must include collection and consent.
    analytics.logInstallAttributionDiagnostic(result) {
        consentStore.mayUploadInstallReferrerDiagnostics()
    }
}
```

`consentStore` represents your own thread-safe policy, not a supplied SGL object. A conservative opt-in policy requires explicit permission for diagnostics, analytics collection enabled, and all four consent dimensions granted. Read and upload policies must account for consent to access the native referrer payload, which can contain advertising identifiers before sanitization. Do not replace either policy with a hardcoded `true`. Both predicates must be fast and must not throw. If consent is unknown or denied, withhold diagnostics. Revocation must affect the predicates immediately.

SGL checks the read policy before starting, retrying, caching or delivering a result and again before the main-thread callback. The upload predicate is checked at event submission, with an additional Test Lab guard. There is no automatic upload and no inferred consent from SDK availability. Native analytics collection controls still apply independently.

### Result fields and provenance

`InstallAttributionResult` exposes a stable `AttributionStatus`, schema version 1, and `play_install_referrer` provenance. Status is also the error category: success, withheld, unsupported, unavailable, developer error, disconnected, timeout, malformed, internal error, or retries exhausted.

Only `utm_source`, `utm_medium`, and `utm_campaign` values can become text fields. Text must be at most 100 characters, contain letters, numbers or the limited punctuation ` _.,:-`, and not equal `(not set)`. URL-shaped, control-character and overlong values are omitted. Duplicate allowlisted keys or broken percent escaping make the result malformed. Unknown URL parameters are ignored. Query input is capped at 8 KiB.

`hasClickId` reports nonempty `gclid`, `gbraid` or `wbraid` presence. `hasCampaignId` reports nonempty `utm_id` or `gad_campaignid` presence, excluding `(not set)`. These flags are not recovered IDs. No click-ID or campaign-ID values, raw URLs, arbitrary parameters or native exception text are persisted, uploaded or logged by SGL.

Client click/install and server click/install timestamps come directly from Play, in Unix seconds. Zero means absent. `installVersion` is Play's original install version, not the current app version. `retrievedSeconds` is the device's wall clock when the successful native read occurred. It is not first launch time, can reflect clock skew, and is unchanged on cached delivery. In existing installs it measures a later diagnostic read, not acquisition. No `first_open` event is created or reset.

The optional `install_attribution_diagnostic` event has at most 14 whitelisted parameters. It never logs `firebase_campaign` or `ad_impression`, changes attribution, or writes user properties. The application owns whether and how often to submit this separate diagnostic event. If recording once per install/schema, persist an upload marker in no-backup storage and suppress repeat cached deliveries. Do not use it as an install or conversion count.

### Execution, caching and limits

A process-wide instance uses application context only. Native work, cache IO and retry state run on a serialized worker. Callbacks always dispatch to the Android main thread, even for cached and withheld results. Policy predicates can run on both threads. Avoid Activity captures in predicates and use lifecycle-aware or weak references in callbacks.

Concurrent requests share one capture. The native service connection is closed after completion, failure or timeout. A connection attempt times out after ten seconds. Transient service errors, disconnects, timeouts and internal errors retry with one- and two-second delays, at most three attempts per installation. Unsupported and developer errors return immediately. Later explicit requests still share that persisted three-attempt budget. A late or duplicate native callback cannot complete a capture twice.

Only sanitized successful results are cached. The cache and attempt budget are in `Context.noBackupFilesDir`, keyed by PackageManager's `firstInstallTime`. Process restarts, Activity recreation and app upgrades reuse the same result. Reinstall, app-data reset, a changed install identity or an invalid cache starts fresh. Restored backup preferences are not used. Storage failures leave the in-memory state usable but cannot guarantee persistence across process death. This is a single-process API; do not call it from multiple Android processes.

Play retains referrers for 90 days and only changes them on reinstall. Old installs may return no useful fields. Unsupported stores, sideloads and devices without Play are normal diagnostic outcomes. Adding this SDK to an existing installation does not create a new acquisition. A second read cannot reveal a campaign ID absent from Play's payload.

This cannot explain all differences between Ads installs and analytics first opens. App code cannot observe users who never launch or bypass consent. GCLIDs are opaque. Google documents that ClickView GCLIDs are unavailable for App Campaigns for Installs and Pre-registration. An empty click export is not proof of a broken transfer. Account links and auto-tagging require separate verification by an authorized administrator.

## Ad lifecycle and impression revenue

The shared `sgl.ads.ObservableAds` capability and `AdObserver` are optional. Existing `AdsProvider.Ads` implementations and forwarding/no-op backends are source-compatible and do not claim native observation support. Keep a reference to the native Android controller to attach an observer before issuing requests. Construct it after the app's advertising consent decision, with `alwaysPreload = false` if attaching before the first request matters:

```kotlin
val ads = AndroidAdMobAds(context, false)
ads.setAdObserver(object : sgl.ads.AdObserver() {
    override fun onPaid(revenue: sgl.ads.AdRevenue) {
        // Observe or forward only to the single revenue owner for this network.
        val currencyUnits = revenue.currencyUnits()
    }
})
// Load only after the app's advertising policy permits an actual request.
```

Callbacks distinguish actual native request, loaded, load failed, impression, dismissal, show failed and paid revenue. Android delivers them on the UI thread. Observers must not block or throw. The game owns opportunity, placement eligibility and scheduling events. Checking readiness or returning `true` from `showInterstitial`/`showRewarded` is not an impression. Reward completion remains separate from revenue and impressions.

`AdRevenue.valueMicros` is the exact native amount. `currencyUnits` divides by 1,000,000. Currency is the SDK currency code; precision uses native codes 0 unknown, 1 estimated, 2 publisher-provided, 3 precise. Metadata includes format, ad unit ID and loaded mediation ad-source name when available, with AdMob as fallback. Game placement labels belong in game code. Each loaded ad has an event adapter that suppresses repeated impression, terminal and paid callbacks. A paid callback can arrive after dismissal and is still reported once. No impression is invented from a paid callback.

Native ads are controller-instance state, not global Activity caches. Activity destruction automatically clears the controller and observer. `destroy()` can release ownership earlier. Do not reuse a controller across Activities; create a new one on recreation.

### Revenue ownership

For linked AdMob and Firebase, Firebase can automatically collect `ad_impression` and revenue. Leave that path as the sole Firebase owner. SGL does not log an analytics event from its observer, and consumers must not also forward these callbacks as Firebase `ad_impression` events.

For another network without automatic logging, select exactly one manual owner and forward its actual impression-paid callback using the analytics custom-event API with the network's required fields and currency units. Do not log revenue from opportunities, requests, show attempts, rewards or dismissal. If automatic collection is enabled later, remove the manual forwarding first. SGL does not enable ads in any consumer or change a game's scheduling.

## Validation

See [the validation record](firebase-attribution-validation.md). Real Play installation, device lifecycle and consent verification are still required before production use. A sideload or a successful build is not proof of Play attribution.
