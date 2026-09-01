load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "sgl-headless-opengl",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    target_compatible_with = ["@platforms//os:linux"],
    visibility = ["//visibility:public"],
    exports = ["//backends/lwjgl-shared:sgl-lwjgl-shared"],
    deps = [
        "//backends/desktop-jvm-shared",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "@maven//:org_lwjgl_lwjgl",
        "@maven//:org_lwjgl_lwjgl_egl",
        "@maven//:org_lwjgl_lwjgl_opengl",
    ],
)

scala_test(
    name = "sgl-headless-opengl-tests",
    srcs = glob(["src/test/**/*.scala"]),
    env = {
        "DISPLAY": "",
        "EGL_PLATFORM": "surfaceless",
        "LIBGL_ALWAYS_SOFTWARE": "1",
        "WAYLAND_DISPLAY": "",
    },
    scalacopts = SGL_SCALACOPTS,
    target_compatible_with = ["@platforms//os:linux"],
    deps = [
        ":sgl-headless-opengl",
        "//backends/desktop-jvm-shared",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
