load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:cross.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "live-config",
    srcs = glob(["src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
)

scala_test(
    name = "tests",
    srcs = glob(["src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        ":live-config",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
