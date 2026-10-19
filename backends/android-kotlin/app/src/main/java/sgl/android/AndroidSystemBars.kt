package sgl.android

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi

/** Android-only window policy. Shared code only sees the resulting safe-area insets. */
internal object AndroidSystemBars {
    fun calculateSafeAreaInsets(windowInsets: WindowInsets): sgl.Insets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val cutout = windowInsets.getInsets(WindowInsets.Type.displayCutout())
            val gestures = windowInsets.getInsets(WindowInsets.Type.systemGestures())
            return sgl.Insets(
                maxOf(bars.left, cutout.left, gestures.left),
                maxOf(bars.top, cutout.top, gestures.top),
                maxOf(bars.right, cutout.right, gestures.right),
                maxOf(bars.bottom, cutout.bottom, gestures.bottom),
            )
        }

        return calculateLegacySafeAreaInsets(windowInsets)
    }

    // API 26 to 29 do not have typed inset queries.
    @Suppress("DEPRECATION")
    private fun calculateLegacySafeAreaInsets(windowInsets: WindowInsets): sgl.Insets {
        var left = windowInsets.stableInsetLeft
        var top = windowInsets.stableInsetTop
        var right = windowInsets.stableInsetRight
        var bottom = windowInsets.stableInsetBottom

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowInsets.displayCutout?.let { cutout ->
                left = maxOf(left, cutout.safeInsetLeft)
                top = maxOf(top, cutout.safeInsetTop)
                right = maxOf(right, cutout.safeInsetRight)
                bottom = maxOf(bottom, cutout.safeInsetBottom)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val gestures = windowInsets.systemGestureInsets
            left = maxOf(left, gestures.left)
            top = maxOf(top, gestures.top)
            right = maxOf(right, gestures.right)
            bottom = maxOf(bottom, gestures.bottom)
        }
        return sgl.Insets(left, top, right, bottom)
    }

    fun apply(
        window: Window,
        mode: AndroidSystemBarsMode,
        behavior: AndroidSystemBarsBehavior,
        navigationBarContrastEnforced: Boolean,
        targetSdkVersion: Int,
    ) {
        // Android 15 enforces edge-to-edge for apps targeting API 35+. In particular,
        // do not call WindowCompat.enableEdgeToEdge: it still calls deprecated APIs.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM ||
            targetSdkVersion < Build.VERSION_CODES.VANILLA_ICE_CREAM
        ) {
            configureLegacyEdgeToEdge(window)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val attributes = window.attributes
            if (attributes.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS) {
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                window.attributes = attributes
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            configureLegacyCutout(window)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = navigationBarContrastEnforced
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController ?: return
            if (mode == AndroidSystemBarsMode.Immersive) {
                controller.systemBarsBehavior = when (behavior) {
                    AndroidSystemBarsBehavior.Default -> defaultSystemBarsBehavior()
                    AndroidSystemBarsBehavior.TransientBySwipe ->
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                controller.hide(WindowInsets.Type.systemBars())
            } else {
                controller.show(WindowInsets.Type.systemBars())
            }
        } else {
            applyLegacySystemBarsVisibility(window, mode, behavior)
        }
        window.decorView.requestApplyInsets()
    }

    // API 31 renamed API 30's BEHAVIOR_SHOW_BARS_BY_SWIPE without changing its value.
    @SuppressLint("InlinedApi")
    private fun defaultSystemBarsBehavior(): Int = android.view.WindowInsetsController.BEHAVIOR_DEFAULT

    // Kept only for devices/apps without platform-enforced edge-to-edge.
    @Suppress("DEPRECATION")
    private fun configureLegacyEdgeToEdge(window: Window) {
        // All modes manage their own insets, including SafeArea's surface margins.
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    // API 28 and 29 cannot draw into cutouts on every edge. Protect content with insets
    // and accept platform letterboxing on long edges rather than use layout hacks.
    @RequiresApi(Build.VERSION_CODES.P)
    @Suppress("DEPRECATION")
    private fun configureLegacyCutout(window: Window) {
        val attributes = window.attributes
        if (attributes.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }
    }

    @Suppress("DEPRECATION")
    private fun applyLegacySystemBarsVisibility(
        window: Window,
        mode: AndroidSystemBarsMode,
        behavior: AndroidSystemBarsBehavior,
    ) {
        val layoutFlags =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = when (mode) {
            AndroidSystemBarsMode.SafeArea -> layoutFlags
            AndroidSystemBarsMode.EdgeToEdge -> layoutFlags
            AndroidSystemBarsMode.Immersive -> layoutFlags or
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                when (behavior) {
                    AndroidSystemBarsBehavior.Default -> android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
                    AndroidSystemBarsBehavior.TransientBySwipe -> android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                }
        }
    }
}
