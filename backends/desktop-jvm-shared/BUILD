load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "desktop-jvm-shared",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "//backends/jvm-shared",
        "//core:sgl-core",
        "@maven//:com_googlecode_soundlibs_vorbisspi",
        "@maven//:net_liftweb_lift_json_2_13",
    ],
)

scala_test(
    name = "desktop-jvm-shared-tests",
    srcs = glob(["src/test/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        ":desktop-jvm-shared",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
