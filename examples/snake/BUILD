load("@rules_java//java:java_test.bzl", "java_test")
load("//bazel/sgl:defs.bzl", "sgl_android_app", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    include_default_modules = False,
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    frame_height = 600,
    frame_width = 600,
    main_class = "Main",
    package = "sgl.examples.snake",
    use_screen2d = False,
    deps = [":core"],
)

sgl_desktop_awt_app(
    name = "desktop-awt-safe-area",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    frame_height = 600,
    frame_width = 600,
    main_class = "SafeAreaMain",
    package = "sgl.examples.snake",
    simulated_safe_area_insets = [
        30,
        50,
        30,
        50,
    ],
    use_screen2d = False,
    deps = [":core"],
)

sgl_android_app(
    name = "android",
    label = "SGL Snake",
    package = "com.regblanc.sgl.snake",
    wiring_expression = "sgl.examples.snake.core.Wiring.wire(platformProxy)",
)

java_test(
    name = "firebase_manifest_test",
    srcs = ["testdata/AndroidFirebaseManifestTest.java"],
    data = [
        ":android-admob-firebase-smoke-debug",
        ":android-firebase-smoke-debug",
    ],
    main_class = "sgl.bazel.android.AndroidFirebaseManifestTest",
    use_testrunner = False,
)

# Offline build fixtures only. The Firebase project and API key are synthetic.
sgl_android_app(
    name = "android-firebase-smoke",
    firebase_ad_personalization = "false",
    firebase_ad_storage = "false",
    firebase_ad_user_data = "false",
    firebase_analytics_collection_enabled = "false",
    firebase_analytics_storage = "false",
    google_services_json = "examples/snake/testdata/google-services.json",
    label = "SGL Firebase smoke",
    package = "sgl.examples.smoke",
    wiring_expression = "sgl.examples.snake.core.Wiring.wire(platformProxy).also { AndroidFirebaseAnalytics(context).setAnalyticsCollectionEnabled(false) }",
)

sgl_android_app(
    name = "android-admob-firebase-smoke",
    admob_always_preload = False,
    admob_application_id = "ca-app-pub-3940256099942544~3347511713",
    firebase_ad_personalization = "false",
    firebase_ad_storage = "false",
    firebase_ad_user_data = "false",
    firebase_analytics_collection_enabled = "false",
    firebase_analytics_storage = "false",
    google_services_json = "examples/snake/testdata/google-services.json",
    label = "SGL AdMob Firebase smoke",
    package = "sgl.examples.smoke",
    wiring_expression = "sgl.examples.snake.core.Wiring.wire(platformProxy).also { AndroidFirebaseAnalytics(context).setAnalyticsCollectionEnabled(false); AndroidAdMobAds(context, false) }",
)

sgl_desktop_native_app(
    name = "desktop-native",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    frame_height = 600,
    frame_width = 600,
    main_class = "Main",
    package = "sgl.examples.snake",
    use_screen2d = False,
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    canvas_height = 600,
    canvas_width = 600,
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    main_class = "SnakeGame",
    package = "sgl.examples.snake",
    use_screen2d = False,
    deps = [":core"],
)
