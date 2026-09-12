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
