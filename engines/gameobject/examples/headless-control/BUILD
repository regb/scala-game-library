load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_test(
    name = "headless-control-integration-test",
    srcs = [
        "HeadlessControlIntegrationSuite.scala",
        "PortableCanvasFontConformanceSuite.scala",
    ],
    env = {
        "DISPLAY": "",
        "EGL_PLATFORM": "surfaceless",
        "LIBGL_ALWAYS_SOFTWARE": "1",
        "WAYLAND_DISPLAY": "",
    },
    scalacopts = SGL_SCALACOPTS,
    target_compatible_with = ["@platforms//os:linux"],
    deps = [
        "//backends/desktop-awt:sgl-desktop-awt",
        "//backends/desktop-jvm-shared",
        "//backends/headless-opengl:sgl-headless-opengl",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "//engines:sgl-gameobject",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
