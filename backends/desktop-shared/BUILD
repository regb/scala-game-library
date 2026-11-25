load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:cross.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

# For every desktop backend, on the JVM and native.
scala_library(
    name = "desktop-shared",
    srcs = glob(["src/main/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = ["//core:sgl-core"],
)

scala_test(
    name = "desktop-shared-tests",
    srcs = glob(["src/test/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        ":desktop-shared",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
