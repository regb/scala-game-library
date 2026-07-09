load(
    "@rules_scala//scala:scala.bzl",
    "scala_library",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name = "desktop-jvm-shared",
    srcs = glob(["src/main/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    deps = [
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "@maven//:net_liftweb_lift_json_2_13",
        "@maven//:com_googlecode_soundlibs_vorbisspi",
    ],
    visibility = ["//visibility:public"],
)
