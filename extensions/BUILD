load("//bazel:cross.bzl", "scala_library")
load("@rules_scala//scala:scala.bzl", "scala_test")

scala_library(
    name="sgl-tiled",
    srcs=glob(["tiled/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-tiled-tests",
    srcs=glob(["tiled/src/test/scala/**/*.scala"]),
    deps = [
        ":sgl-tiled",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_2_13",
    ],
    visibility=["//visibility:public"],
)
