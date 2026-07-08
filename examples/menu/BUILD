load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")
load("//bazel:cross.bzl", sgl_scala_library = "scala_library")
load("//bazel:scalanative.bzl", "scala_native_binary")

sgl_core_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "com.regblanc.sgl.menu",
    main_class = "Main",
    core_abstract_class = "com.regblanc.sgl.menu.core.AbstractApp",
    deps = [":core"],
    frame_width = 400,
    frame_height = 600,
)

sgl_html5_app(
    name = "html5",
    package = "com.regblanc.sgl.menu",
    main_class = "MenuGame",
    deps = [":core"],
    core_abstract_class = "com.regblanc.sgl.menu.core.AbstractApp",
    canvas_width = 400,
    canvas_height = 600,
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
    main_class = "com.regblanc.sgl.menu.desktop.Main",
)
