load("@rules_scala//scala:scala.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel/sgl:defs.bzl", "sgl_desktop_awt_app")

scala_library(
    name = "game",
    srcs = ["App.scala"],
    scalacopts = SGL_SCALACOPTS,
    deps = ["//core:sgl-core"],
)

scala_library(
    name = "development",
    srcs = ["Development.scala"],
    scalacopts = SGL_SCALACOPTS,
    deps = [
        ":game",
        "//core:sgl-core",
        "//modules/live-config",
        "//modules/live-config-jvm",
    ],
)

sgl_desktop_awt_app(
    name = "desktop-dev",
    core_abstract_class = "sgl.examples.liveconfig.Development",
    frame_height = 240,
    frame_width = 640,
    main_class = "Main",
    package = "sgl.examples.liveconfig.dev",
    use_screen2d = False,
    deps = [
        ":development",
        ":game",
        "//modules/live-config",
        "//modules/live-config-jvm",
    ],
)

sgl_desktop_awt_app(
    name = "desktop-release",
    core_abstract_class = "sgl.examples.liveconfig.AbstractApp",
    frame_height = 240,
    frame_width = 640,
    main_class = "Main",
    package = "sgl.examples.liveconfig.release",
    use_screen2d = False,
    deps = [":game"],
)
