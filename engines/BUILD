load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:cross.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "sgl-screen2d",
    srcs = glob(["screen2d/src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
    ],
)

scala_library(
    name = "sgl-gameobject",
    srcs = glob(["gameobject/src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
    ],
)

scala_test(
    name = "sgl-gameobject-tests",
    srcs = glob(["gameobject/src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        ":sgl-gameobject",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
