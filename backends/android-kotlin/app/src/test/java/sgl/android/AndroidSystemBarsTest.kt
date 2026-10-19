package sgl.android

import android.app.Activity
import android.graphics.Color
import android.graphics.Insets
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26, 28, 29, 30, 34, 35])
class AndroidSystemBarsTest {
    @Suppress("DEPRECATION")
    @Test
    fun windowConfigurationPreservesCompatibility() {
        val controller = Robolectric.buildActivity(Activity::class.java)
        val activity = controller.get()
        activity.setTheme(android.R.style.Theme_Material_NoActionBar)
        controller.setup()
        try {
            val window = activity.window
            window.statusBarColor = Color.RED
            window.navigationBarColor = Color.BLUE
            val initialStatusColor = window.statusBarColor
            val initialNavigationColor = window.navigationBarColor

            AndroidSystemBars.apply(
                window, AndroidSystemBarsMode.EdgeToEdge,
                AndroidSystemBarsBehavior.TransientBySwipe, false, 37,
            )

            if (android.os.Build.VERSION.SDK_INT >= 35) {
                // Enforced edge-to-edge needs neither color setter.
                assertEquals(initialStatusColor, window.statusBarColor)
                assertEquals(initialNavigationColor, window.navigationBarColor)
            } else {
                assertEquals(Color.TRANSPARENT, window.statusBarColor)
                assertEquals(Color.TRANSPARENT, window.navigationBarColor)
            }
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                assertEquals(
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS,
                    window.attributes.layoutInDisplayCutoutMode,
                )
            } else if (android.os.Build.VERSION.SDK_INT >= 28) {
                assertEquals(
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES,
                    window.attributes.layoutInDisplayCutoutMode,
                )
            }
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                assertEquals(false, window.isNavigationBarContrastEnforced)
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Suppress("DEPRECATION")
    @Config(sdk = [26, 28, 29])
    @Test
    fun legacyModesRestoreVisibleBarsAfterImmersive() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val window = controller.get().window
            AndroidSystemBars.apply(
                window, AndroidSystemBarsMode.Immersive,
                AndroidSystemBarsBehavior.TransientBySwipe, false, 37,
            )
            val hiddenFlags = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            assertEquals(hiddenFlags, window.decorView.systemUiVisibility and hiddenFlags)
            assertEquals(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY,
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY,
            )
            for (mode in listOf(AndroidSystemBarsMode.SafeArea, AndroidSystemBarsMode.EdgeToEdge)) {
                AndroidSystemBars.apply(window, mode, AndroidSystemBarsBehavior.Default, false, 37)
                assertEquals(0, window.decorView.systemUiVisibility and hiddenFlags)
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Config(sdk = [34, 35])
    @Test
    fun modernModesRestoreVisibleBarsAfterImmersive() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val window = controller.get().window
            val bars = WindowInsets.Type.systemBars()
            for (behavior in AndroidSystemBarsBehavior.entries) {
                AndroidSystemBars.apply(window, AndroidSystemBarsMode.Immersive, behavior, false, 37)
                val hiddenTypes = ReflectionHelpers.callInstanceMethod<Int>(
                    window.insetsController, "getRequestedVisibleTypes",
                )
                assertEquals(0, hiddenTypes and bars)
                assertEquals(
                    if (behavior == AndroidSystemBarsBehavior.Default) 1 else 2,
                    window.insetsController!!.systemBarsBehavior,
                )
                for (mode in listOf(AndroidSystemBarsMode.SafeArea, AndroidSystemBarsMode.EdgeToEdge)) {
                    AndroidSystemBars.apply(window, mode, behavior, true, 37)
                    val visibleTypes = ReflectionHelpers.callInstanceMethod<Int>(
                        window.insetsController, "getRequestedVisibleTypes",
                    )
                    assertEquals(bars, visibleTypes and bars)
                    assertEquals(true, window.isNavigationBarContrastEnforced)
                }
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Suppress("DEPRECATION")
    @Config(sdk = [35])
    @Test
    fun olderTargetSdkStillGetsCompatibilitySetupOnNewDevices() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val window = controller.get().window
            window.statusBarColor = Color.RED
            window.navigationBarColor = Color.BLUE
            AndroidSystemBars.apply(
                window, AndroidSystemBarsMode.EdgeToEdge,
                AndroidSystemBarsBehavior.Default, false, 34,
            )
            assertEquals(Color.TRANSPARENT, window.statusBarColor)
            assertEquals(Color.TRANSPARENT, window.navigationBarColor)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Config(sdk = [30, 34, 35])
    @Test
    fun safeInsetsReserveHiddenBarsAndCombineCutoutsAndGestures() {
        val insets = WindowInsets.Builder()
            .setInsetsIgnoringVisibility(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 48))
            .setVisible(WindowInsets.Type.systemBars(), false)
            .setInsets(WindowInsets.Type.displayCutout(), Insets.of(40, 32, 0, 0))
            .setVisible(WindowInsets.Type.displayCutout(), true)
            .setInsets(WindowInsets.Type.systemGestures(), Insets.of(16, 0, 20, 64))
            .setVisible(WindowInsets.Type.systemGestures(), true)
            .build()

        val safeArea = AndroidSystemBars.calculateSafeAreaInsets(insets)
        assertEquals(40, safeArea.left())
        assertEquals(32, safeArea.top())
        assertEquals(20, safeArea.right())
        assertEquals(64, safeArea.bottom())
    }
}
