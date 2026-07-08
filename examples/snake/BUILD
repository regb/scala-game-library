load("//bazel/sgl:defs.bzl", "sgl_android_app", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")
load("//bazel:cross.bzl", sgl_scala_library = "scala_library")
load("//bazel:scalanative.bzl", "scala_native_binary")

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

sgl_scala_library(
    name = "desktop-native-lib",
    srcs = ["desktop-native/src/main/scala/Main.scala"] + ["//desktop-native:srcs"],
    tags = ["manual"],
    deps = [
        ":core",
        "//core:sgl-core",
        "//modules:sgl-scene2d",
        "//modules:sgl-particles",
        "//jvm-shared:jvm-shared",
    ],
)

scala_native_binary(
    name = "desktop-native",
    tags = ["manual"],
    deps = [":desktop-native-lib"],
    main_class = "com.regblanc.sgl.snake.desktop.Main",
)
