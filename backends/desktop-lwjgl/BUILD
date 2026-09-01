load("@rules_scala//scala:scala.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "sgl-desktop-lwjgl",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    exports = ["//backends/lwjgl-shared:sgl-lwjgl-shared"],
    deps = [
        "//backends/desktop-jvm-shared",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "@maven//:org_lwjgl_lwjgl",
        "@maven//:org_lwjgl_lwjgl_glfw",
        "@maven//:org_lwjgl_lwjgl_openal",
        "@maven//:org_lwjgl_lwjgl_opengl",
    ] + select({
        "@platforms//os:linux": [
            "@maven//:org_lwjgl_lwjgl_glfw_natives_linux",
            "@maven//:org_lwjgl_lwjgl_glfw_natives_linux_arm64",
            "@maven//:org_lwjgl_lwjgl_openal_natives_linux",
            "@maven//:org_lwjgl_lwjgl_openal_natives_linux_arm64",
        ],
        "@platforms//os:windows": [
            "@maven//:org_lwjgl_lwjgl_glfw_natives_windows",
            "@maven//:org_lwjgl_lwjgl_openal_natives_windows",
        ],
        "@platforms//os:macos": [
            "@maven//:org_lwjgl_lwjgl_glfw_natives_macos",
            "@maven//:org_lwjgl_lwjgl_glfw_natives_macos_arm64",
            "@maven//:org_lwjgl_lwjgl_openal_natives_macos",
            "@maven//:org_lwjgl_lwjgl_openal_natives_macos_arm64",
        ],
        "//conditions:default": [],
    }),
)
