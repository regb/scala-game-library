load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "sgl-desktop-lwjgl",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        "//core:sgl-core",
        "//desktop-jvm-shared:desktop-jvm-shared",
        "@maven//:org_lwjgl_lwjgl",
        "@maven//:org_lwjgl_lwjgl_glfw",
        "@maven//:org_lwjgl_lwjgl_opengl",
        "@maven//:org_lwjgl_lwjgl_openal",
        "@maven//:org_lwjgl_lwjgl_stb",
        "@maven//:org_lwjgl_lwjgl_natives_linux",
        "@maven//:org_lwjgl_lwjgl_glfw_natives_linux",
        "@maven//:org_lwjgl_lwjgl_opengl_natives_linux",
        "@maven//:org_lwjgl_lwjgl_openal_natives_linux",
        "@maven//:org_lwjgl_lwjgl_stb_natives_linux",
    ],
    visibility = ["//visibility:public"],
)
