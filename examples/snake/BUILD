load("//bazel/sgl:defs.bzl", "sgl_android_app", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala", "MainScreen.scala"],
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "com.regblanc.sgl.snake",
    main_class = "Main",
    core_abstract_class = "com.regblanc.sgl.snake.core.AbstractApp",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    package = "com.regblanc.sgl.snake",
    main_class = "SnakeGame",
    deps = [":core"],
    core_abstract_class = "com.regblanc.sgl.snake.core.AbstractApp",
)

sgl_android_app(
    name = "android",
    package = "com.regblanc.sgl.snake",
    label = "SGL Snake",
)
