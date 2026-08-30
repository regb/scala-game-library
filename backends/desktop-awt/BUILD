load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "sgl-desktop-awt",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "//backends/desktop-jvm-shared",
        "//backends/jvm-shared",
        "//core:sgl-core",
        "//engines:sgl-screen2d",
    ],
)

scala_test(
    name = "sgl-desktop-awt-tests",
    srcs = glob(["src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        ":sgl-desktop-awt",
        "//backends/desktop-jvm-shared",
        "//backends/jvm-shared",
        "//core:sgl-core",
        "//core:sgl-core-testlib",
        "//engines:sgl-screen2d",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
