load("//bazel/sgl:defs.bzl", "sgl_android_app", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    include_default_modules = False,
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "sgl.examples.snake",
    main_class = "Main",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    deps = [":core"],
    use_screen2d = False,
    frame_width = 600,
    frame_height = 600,
)

sgl_android_app(
    name = "android",
    package = "com.regblanc.sgl.snake",
    label = "SGL Snake",
    wiring_expression = "sgl.examples.snake.core.Wiring.wire(platformProxy)",
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.snake",
    main_class = "Main",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    deps = [":core"],
    use_screen2d = False,
    frame_width = 600,
    frame_height = 600,
)

sgl_html5_app(
    name = "html5",
    package = "sgl.examples.snake",
    main_class = "SnakeGame",
    core_abstract_class = "sgl.examples.snake.core.AbstractApp",
    deps = [":core"],
    use_screen2d = False,
    canvas_width = 600,
    canvas_height = 600,
)
