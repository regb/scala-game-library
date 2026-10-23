# Firebase integration validation

Validated in this item's managed SGL checkout on October 23, 2026, starting at `57d60c7`. The local integration branch was `master` at that revision. No integration branch, consumer pin, cloud setting or release was changed.

## Dependency checks

- Official Firebase Android release notes listed BoM 35.1.0 and Analytics 24.0.0. Analytics' major-version change raises its minimum Android API to 24, below SGL's retained API 26 minimum.
- Google's Maven metadata still listed Install Referrer 2.2 as current stable.
- Gradle resolved Analytics and Play Services Measurement 24.0.0 under BoM 35.1.0. The initial build rejected their Kotlin 2.4 metadata with AGP's default compiler. Raising the built-in Kotlin dependency to 2.4.21 fixed both library and generated-project compilation.
- The backend uses the main Firebase artifact rather than the removed standalone KTX modules.

## Passed checks

```bash
bazel run @buildifier_prebuilt//:buildifier -- -mode=check -lint=off -r .
bazel run @buildifier_prebuilt//:buildifier -- -mode=check -lint=warn -r .
bazel build //...

ANDROID_HOME=/home/regb/Android/Sdk backends/android-kotlin/gradlew \
  -p backends/android-kotlin \
  :sgl-android:testDebugUnitTest \
  :sgl-android-firebase:testDebugUnitTest \
  :sgl-android-admob:testDebugUnitTest \
  :sgl-android-firebase:lintDebug \
  :sgl-android-admob:lintDebug
```

There are 35 passing Kotlin tests: 16 existing Android tests, 15 Firebase/referrer tests and four ad lifecycle tests. New deterministic tests cover allowlisted parsing, URL encoding, missing/malformed/duplicate/overlong fields, discarded IDs, diagnostic policy gating, reserved-event avoidance, successful capture coalescing and caching, process recreation, install identity changes, data reset, corrupted storage, bounded retry/timeout behavior, successful retry, native exceptions, duplicate/late callbacks, cleanup, consent changes during native reads/retries, and main-thread delivery with a final revocation check.

Consent tests use an injectable fake native client. They verify all four native consent keys, partial updates, collection independence, Test Lab enforcement and delegate recreation over persisted fake state. They do not independently establish Google's on-device persistence implementation. That behavior relies on Firebase's documented contract and needs a device check.

Ad event-adapter tests verify no synthetic impressions, callback ordering, releasing terminal state before observer re-entry, exactly-once terminal/impression/paid delivery and micros-to-currency conversion. They do not exercise real ads or real mediation adapters. Lint passes with dependency-version/catalog warnings.

The new Bazel `//examples/snake:firebase_manifest_test` validates generated consent metadata, optional module selection and Kotlin configuration. Existing generated-project tests verify that unset consent defaults remain absent.

## Generated APK smoke builds

Both debug fixtures built successfully with synthetic, non-production Firebase configuration and denied manifest consent defaults:

```bash
ANDROID_HOME=/home/regb/Android/Sdk bazel run //examples/snake:android-firebase-smoke-debug
ANDROID_HOME=/home/regb/Android/Sdk bazel run //examples/snake:android-admob-firebase-smoke-debug
ANDROID_HOME=/home/regb/Android/Sdk bazel run //examples/snake:android-admob-firebase-smoke-release
```

The combined release build runs R8 and resource shrinking. `apkanalyzer` reports minimum SDK 26 and target SDK 37. A final repeat exhausted the accumulated Gradle daemon's 1 GiB metaspace budget and hit the command timeout. A clean daemon with the following local JVM override completed the same final generated release project successfully:

```bash
ANDROID_HOME=/home/regb/Android/Sdk backends/android-kotlin/gradlew \
  -p .bazel-android/examples/snake/android-admob-firebase-smoke \
  :app:assembleRelease -PsglBazelRepo="$PWD" \
  '-Dorg.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=2g -Dfile.encoding=UTF-8'
```

A validation-only copy of the generated Firebase-only app also assembled with the optional BoM-managed Crashlytics SDK dependency. This does not verify Crashlytics runtime initialization, mapping upload or the Crashlytics Gradle plugin in a consumer release pipeline. The fixture JSON does not point to a usable Firebase project, and no app was installed or launched.

## Consumer source compatibility

Rat Trap and WinSmash were copied into isolated build-only snapshots inside this checkout, with local SGL module overrides and synthetic Firebase configuration. Their existing Scala sources and generated Android wiring compiled and assembled debug APKs against this implementation:

- Rat Trap's `Wiring.wireAndroid(platformProxy, AndroidSave("rattrap", context), AndroidFirebaseAnalytics(context))`.
- WinSmash's `Wiring.wire(platformProxy, AndroidFirebaseAnalytics(context))`.

The original working trees and pins were not modified. Rat Trap's analytics/report edits were not used or changed. A broken drawable symlink in WinSmash's source resource tree was omitted from the snapshot; this does not establish completeness of its packaged game assets. These are compatibility builds, not consumer deployments or release acceptance tests. Temporary snapshots were moved out of SGL's recursive Bazel package scan after validation.

## Repository test limitations

`bazel test //...` passed 11 of 13 targets, including the new manifest test. These two existing targets could not load `libEGL.so.1` in the Nix Java environment:

- `//backends/headless-opengl:sgl-headless-opengl-tests`
- `//engines/gameobject/examples/headless-control:headless-control-integration-test`

Trying the host library directory instead caused a host/Nix GLIBC version mismatch before Java startup. No source workaround was made for these environment failures.

## Consumer-review follow-up: guard the native getter

Rat Trap's review identified that the successful service-setup callback read `client.installReferrer` before rechecking the read policy. The existing tests established post-read suppression, but did not prevent that payload access.

The follow-up is based on upstream `c963e6c112e6ee834435c6a091f5d5626820d00c`, preserving desktop audio commit `3cd584e`. `PlayClient` now receives a live read guard. The capture worker publishes immutable snapshots of the coalesced policy predicates; the native callback evaluates their current choices immediately before invoking the payload getter. At least one permitted request authorizes the read. Each revoked request still receives only `WITHHELD`. The connection's generation must also remain current, preventing a timed-out callback from reading under a retry's permission. Duplicate setup callbacks cannot invoke the getter twice.

An injectable `PlayReferrerConnection` wraps the actual native getter rather than an already-read payload. Nine new tests exercise the production `PlayClient` setup callback through this boundary, including:

- Revocation while connecting: zero getter calls, `WITHHELD`, no cache and one connection close.
- A later coalesced request remains allowed: one getter call, successful sanitized capture for that request, and withholding for the revoked request.
- All coalesced requests revoked; late callbacks from timed-out attempts; native getter errors and bounded retries; error responses without payload access; revocation during an in-flight read; duplicate setup callbacks.
- A blocked native getter does not block the capture worker's timeout or allow late delivery.

Temporarily bypassing the pre-getter guard made `revokingWhileConnectingPreventsNativeGetterAccess` fail because the getter was invoked. The guard was restored and the full test suite rerun successfully.

Follow-up validation passed both prescribed Buildifier checks, `bazel build //...`, Firebase lint, all 44 Kotlin tests across Android/Firebase/AdMob, and the generated Firebase-only debug APK build. Of those tests, 24 cover Firebase/referrer behavior. `bazel test //...` again passed 11 of 13 targets; the same two OpenGL targets failed on unavailable `libEGL.so.1`. Desktop audio tests passed with the preserved upstream changes.

This follow-up does not change public setup, consent defaults, manifest permissions, diagnostic opt-in behavior, storage format, retry budget or callback delivery threads. No consumer files, pins, cloud settings, ads or diagnostic enablement were changed. Real Play/device verification remains outstanding. The fix is prepared as a local commit for review, not a new release or consumer deployment.

## Production checks still required

Use a real Play install to verify native referrer status, client/server timestamps, original install version, and behavior after an upgrade. Sideload results cannot establish Play attribution. Verify denied/unknown consent before Firebase auto-initialization, each consent dimension's updates and persistence across process death, collection toggles, cached-data withholding after revocation, reinstall/data-reset behavior, and Activity recreation on devices.

For ads, test actual native load/show failures, impressions, rewarded completion, dismissal and paid callbacks on devices, including mediation and Activity destruction while loading/showing. Confirm that linked AdMob/Firebase automatic `ad_impression` collection has a single owner before enabling any manual forwarding. No ads were enabled in Rat Trap.

No GA4/Ads account settings were inspected or changed. No game, SDK artifact or release was published, merged or deployed. Missing campaign-ID coverage and Ads-install/first-open discrepancies remain attribution limitations, not demonstrated SDK bugs fixed by this work.
