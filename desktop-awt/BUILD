load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_library",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name="sgl-desktop-awt",
    srcs=glob(["src/main/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps=[
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "@maven//:net_liftweb_lift_json_2_13",
        "@maven//:com_googlecode_soundlibs_vorbisspi",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-desktop-awt-tests",
    srcs=glob(["src/test/scala/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps = [
        ":sgl-desktop-awt",
        "//core:sgl-core",
        "//core:sgl-core-testlib",
        "//jvm-shared:jvm-shared",
        "@maven//:org_scalatest_scalatest_2_13",
    ],
    visibility=["//visibility:public"],
)
