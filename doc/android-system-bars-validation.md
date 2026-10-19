# Android system-bar validation

The backend keeps API 26 support. Android 15 and later use enforced edge-to-edge when the app targets API 35 or later. Older devices and older-targeting custom apps keep compatibility setup. Generated apps target API 37 and must not opt out of edge-to-edge enforcement.

## Automated checks

- `bazel build //...` passed.
- Buildifier checks passed.
- `bazel test //...` passed 10 of 12 targets, including core and Android project-generation tests. The two headless OpenGL targets failed because LWJGL could not locate `libEGL.so.1` in the test environment.
- Android backend `:sgl-android:testDebugUnitTest` passed 15 Robolectric cases on API 26, 28, 29, 30, 34, and 35. Coverage includes compatibility setup, cutout policy, contrast control, restoring visible bars after immersive mode, both modern reveal behaviors, older target SDKs on Android 15, and combining stable bar insets with cutout and gesture insets.
- Android backend `:sgl-android:lintDebug` passed. The backend has other existing lint warnings.
- `bazel run //examples/snake:android-debug` assembled a debug APK.
- `bazel run //examples/snake:android-release` assembled a minified unsigned release APK. Its manifest reports minimum SDK 26 and target SDK 37.

Run backend tests with an Android SDK configured:

```bash
backends/android-kotlin/gradlew -p backends/android-kotlin \
  :sgl-android:testDebugUnitTest :sgl-android:lintDebug
```

## Release artifact audit

The Snake release APK is generated at:

```text
.bazel-android/examples/snake/android/app/build/outputs/apk/release/app-release-unsigned.apk
```

Its R8 mapping is under the same `outputs` directory at `mapping/release/mapping.txt`.

A full DEX disassembly scan for calls to `setStatusBarColor`, `setNavigationBarColor`, `setDecorFitsSystemWindows`, and `setSystemUiVisibility` found one call site for each. All four belonged to SGL compatibility code, including an R8-generated API outline. There were no dependency-owned call sites for these four methods in this example. The disassembly retained the runtime and target SDK checks that bypass the color setters and decor-fitting call when both SDKs are at least 35. Legacy UI visibility is used only below API 30.

This audit does not cover optional Ads, Firebase, or Play Games integrations. Their final app artifacts need separate inspection. No WinSmash release mapping was available here, so the original `d3.k0.a` report remains unattributed.

AndroidX Core 1.17.0's `WindowCompat.enableEdgeToEdge` was also inspected. It invokes both deprecated color setters and delegates to deprecated decor-fitting setup. SGL does not use that helper for its modern path.

## Device checks still required

Robolectric tests do not establish visual correctness on devices. Test API 26 and Android 15 or later with all three system-bar modes. On modern devices, cover gesture and three-button navigation, cutouts, transient bar reveal, focus loss and regain, pause and resume, and Back. Check that full-window backgrounds reach every supported edge, controls stay within `Window.safeArea`, and game-painted unsafe areas remain game-owned. API 28 and 29 may letterbox long-edge cutouts.

Play's general edge-to-edge compatibility recommendation and its concrete deprecated-API warning are separate. Compatibility calls remain in the release artifact, so a deprecated-API warning may remain. Neither warning can be declared cleared before Play analyzes a newly uploaded release. No upload or WinSmash release is part of this change.
