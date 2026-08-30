load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "jvm-shared",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
    ],
)

scala_test(
    name = "jvm-shared-tests",
    srcs = glob(["src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        ":jvm-shared",
        "//core:sgl-core",
        "//core:sgl-core-testlib",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
