load("@rules_scala//scala:scala.bzl", "scala_library", "scala_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "live-config-jvm",
    srcs = glob(["src/main/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = ["//modules/live-config"],
)

scala_test(
    name = "tests",
    srcs = glob(["src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        ":live-config-jvm",
        "//modules/live-config",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
