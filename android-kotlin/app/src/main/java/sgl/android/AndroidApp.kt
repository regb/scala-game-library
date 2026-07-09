package sgl.android


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.window.OnBackInvokedCallback
import sgl.`Input$`
import sgl.InputActions
import sgl.proxy.ProxiedGameApp

/** Activity providing all providers implementation for Android.
 *
 * This abstract Activity implements basic lifecycle methods of Android
 * Activities and also mix-in all the cake providers from SGL.
 * The role of this is to initialize a SurfaceView as the root view of
 * the activity and start a game loop invoking the update/render functions,
 * rendering to the SurfaceView.
 */
enum class AndroidSystemBarsMode {
    SafeArea,
    EdgeToEdge,
    Immersive,
}

enum class AndroidSystemBarsBehavior {
    Default,
    TransientBySwipe,
}

open class BaseMainActivity(val makeGameApp: (ctx: Context, platform: AndroidPlatformProxy) -> ProxiedGameApp): Activity() {


    /** Controls how the game surface interacts with Android system bars. */
    var SystemBarsMode: AndroidSystemBarsMode = AndroidSystemBarsMode.Immersive

    /** Controls how hidden system bars are revealed in immersive mode. */
    var SystemBarsBehavior: AndroidSystemBarsBehavior = AndroidSystemBarsBehavior.TransientBySwipe

    /** Controls Android's navigation-bar contrast scrim in edge-to-edge modes. */
    var NavigationBarContrastEnforced: Boolean = false

    /** Control if the screen should always stay on.
     *
     * Control the flag FLAG_KEEP_SCREEN_ON, if set to true, as long as we are
     * within the game activity, the app will not go to sleep (probably what the
     * player expect). Active by default, override to change.
     */
    var KeepScreenOn: Boolean = true

    /** Indicates if the activity is in running state
     *
     * This checks that the activity is between calls
     * onResume and onPause. We use this in the SurfaceHolder.Callback
     * to check if we should fire up a resume event for the app.
     */
    @Volatile
    var appResumed: Boolean = false

    /** Indicates if the surface is ready to be drawn on
     *
     * This is set after the SurfaceCreated callback and
     * means that we can start drawing on the surface. It
     * should be used in conjunction with the appResumed
     * flag to control when to run the game loop.
     */
    @Volatile
    var surfaceReady: Boolean = false

    var gameLoop: GameLoop? = null
    var gameLoopThread: Thread? = null

    var gameView: GameView? = null

    var gameApp: ProxiedGameApp? = null
    @Volatile
    var applicationStarted: Boolean = false

    var platformProxy: AndroidPlatformProxy? = null

    private var backInvokedCallback: OnBackInvokedCallback? = null

    //private implicit val LogTag: Logger.Tag = Logger.Tag("sgl-main-activity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //logger.trace("onCreated called")

        appResumed = false
        surfaceReady = false

        gameView = GameView(this, null)
        gameView?.setOnTouchListener(AndroidInputListener(this))
        setGameContentView(gameView!!)

        // Create the platform proxy first
        platformProxy = AndroidPlatformProxy(this, gameView!!)
        gameApp = makeGameApp(this, platformProxy!!)

        /* make sure that if the activity is launched again and the previous
         * one is still around, it will finish the new one and use the existing one
         */
        val intent = getIntent()
        if(!isTaskRoot() && intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            Intent.ACTION_MAIN.equals(intent.getAction())) {
            //logger.warning("Main Activity is not the root.  Finishing Main Activity instead of launching.")
            finish()
            // With this call to finish, it seem that onDestroy will be called before any other lifecycle methods (onResume, onPause).
            // In particular, we need to be careful with the Scheduler shutdown.
            // finish() does not stop the invokation of onCreate, but it will call onDestroy right after. So we do not return
            // in order to maintain the symmetry between onCreate and onDestroy.
        }

        if(KeepScreenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        registerModernBackHandler()
        applySystemBarsConfiguration()

        // Startup is deferred until the drawing surface is ready.
    }

    private fun setGameContentView(view: GameView) {
        val container = FrameLayout(this)
        container.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        container.setOnApplyWindowInsetsListener { _, windowInsets ->
            val (left, top, right, bottom) = if (SystemBarsMode == AndroidSystemBarsMode.SafeArea) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val insets = windowInsets.getInsets(
                        WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                    )
                    listOf(insets.left, insets.top, insets.right, insets.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    listOf(
                        windowInsets.systemWindowInsetLeft,
                        windowInsets.systemWindowInsetTop,
                        windowInsets.systemWindowInsetRight,
                        windowInsets.systemWindowInsetBottom,
                    )
                }
            } else {
                listOf(0, 0, 0, 0)
            }

            val params = view.layoutParams as FrameLayout.LayoutParams
            if (
                params.leftMargin != left ||
                params.topMargin != top ||
                params.rightMargin != right ||
                params.bottomMargin != bottom
            ) {
                params.setMargins(left, top, right, bottom)
                view.layoutParams = params
            }
            windowInsets
        }
        setContentView(container)
        container.requestApplyInsets()
    }

    @Suppress("DEPRECATION")
    private fun applySystemBarsConfiguration() {
        if (SystemBarsMode != AndroidSystemBarsMode.SafeArea) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = NavigationBarContrastEnforced
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController ?: return
            if (SystemBarsMode == AndroidSystemBarsMode.Immersive) {
                controller.systemBarsBehavior = when (SystemBarsBehavior) {
                    AndroidSystemBarsBehavior.Default ->
                        android.view.WindowInsetsController.BEHAVIOR_DEFAULT
                    AndroidSystemBarsBehavior.TransientBySwipe ->
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                controller.hide(WindowInsets.Type.systemBars())
            }
        } else {
            val layoutFlags =
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.decorView.systemUiVisibility = when (SystemBarsMode) {
                AndroidSystemBarsMode.SafeArea -> layoutFlags
                AndroidSystemBarsMode.EdgeToEdge -> layoutFlags
                AndroidSystemBarsMode.Immersive -> layoutFlags or
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                    android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    when (SystemBarsBehavior) {
                        AndroidSystemBarsBehavior.Default -> android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
                        AndroidSystemBarsBehavior.TransientBySwipe -> android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySystemBarsConfiguration()
    }

    override fun onDestroy() {
        stopGameLoop()
        try {
            // Let the game release sounds and music before shutting down platform audio.
            if (applicationStarted) {
                gameApp?.shutdown()
                applicationStarted = false
            }
        } finally {
            try {
                platformProxy?.let { platform ->
                    (platform.audioProxy() as? AndroidAudioProxy)?.dispose()
                }
            } finally {
                unregisterModernBackHandler()
                (platformProxy?.schedulerProxy() as? AndroidSchedulerProxy)?.shutdown()
                super.onDestroy()
            }
        }
    }

    private fun stopGameLoop() {
        gameLoop?.running = false
        val thread = gameLoopThread
        if (thread != null && thread !== Thread.currentThread()) {
            try {
                thread.join(2000L)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        gameLoop = null
        gameLoopThread = null
    }

    // Although we use flags for the various state, we should
    // still start the game loop in the onResume (and check other flags as part
    // of the game loop to know if we can actually render a frame or not). We
    // should use onResume and onPause to start/pause things such as game loops, worker
    // threads, and music. It is safe as we know that these callbacks will always be
    // invoked when the user loses focus for a significant amount of time.
    override fun onResume() {
        super.onResume()
        applySystemBarsConfiguration()

        appResumed = true

        gameLoop = GameLoop(this, gameApp!!)
        val t = Thread(gameLoop)
        gameLoopThread = t
        t.start()
        (platformProxy?.schedulerProxy() as? AndroidSchedulerProxy)?.resume()

        platformProxy?.let { platform ->
            val audioProxy = platform.audioProxy()
            if (audioProxy is AndroidAudioProxy) {
                audioProxy.resumeAllMusic()
            }
        }

        // TODO: maybe the lifecycle resume event should be more precise and take into account
        //       things like surfaceReady and focus flags.
        if (applicationStarted) gameApp?.resume()
    }

    override fun onPause() {
        super.onPause()

        appResumed = false

        platformProxy?.let { platform ->
            val audioProxy = platform.audioProxy()
            if (audioProxy is AndroidAudioProxy) {
                audioProxy.pauseAllMusic()
            }
        }

        stopGameLoop()
        (platformProxy?.schedulerProxy() as? AndroidSchedulerProxy)?.pause()

        if (applicationStarted) gameApp?.pause()
    }

    /** Enable the back button events.
     *
     * If set to true, explicitly handles the back button pressed event.
     * Otherwise, the system handles it by default and close the Activity.
     */
    var EnableBackButtonEvents = false

    var EnableMenuButtonEvents = false

    private fun handleBackPressed() {
        if(EnableBackButtonEvents) {
            `Input$`.`MODULE$`.inputProcessor().systemAction(InputActions.`Back$`())
        } else {
            finish()
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if(EnableBackButtonEvents) {
            `Input$`.`MODULE$`.inputProcessor().systemAction(InputActions.`Back$`())
        } else {
            super.onBackPressed()
        }
    }

    private fun registerModernBackHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backInvokedCallback == null) {
            val callback = OnBackInvokedCallback { handleBackPressed() }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
            backInvokedCallback = callback
        }
    }

    private fun unregisterModernBackHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backInvokedCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            backInvokedCallback = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_MENU && EnableMenuButtonEvents) {
            `Input$`.`MODULE$`.inputProcessor().systemAction(InputActions.`Menu$`())
            return true
        }

        // Important to call super.onKeyDown, because the default implementation handles
        // the onBackPressed event. We also cannot just return false as is usual for
        // a chain of onKeyDown, because we are actually overriding the base activity
        // method, so there's no outer code that will check the result and call the
        // base implementation on false.
        return super.onKeyDown(keyCode, event)
    }


    // TODO: Provide a config control for Portrait/Landscape/Locked, which is
    //  optional (if not used, do nothing, which means that the user can just
    //  configure its AndroidManifest). We should provide a way to give constraints
    //  (min width, max width) under which to force which mode. For example, maybe
    //  we force portrait for a phone (up to 500dp) but then we let portrait/landscape
    //  if screen is larger, but we still lock it in place (no rotation). These combination
    //  of constraints are impossible to specify in the AndroidManifest only, but we
    //  can ensure them programatically.

    // TODO: handle config changes in a way transparent to the framework.
    //       * The window size change should be already handled from the GameView surface
    //       * Orientation change should be notified somehow (isn't it just a height/width change?)
    //       * Locale for text translations
    //    The other thing to consider is that the Manifest should contain a configChanges attribute
    //    that must list all the config change that should be notified with this callback. Any config
    //    change that is not listed there will not be notified and instead trigger an Activity restart.
    //    As we certainly don't want an activity restart, it would be nice to automatically generate this
    //    configChanges attribute so that games don't need to worry about.
    // override def onConfigurationChange(newConfig: Configuration): Unit = {}

}

