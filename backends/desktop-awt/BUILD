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
        "//engines:sgl-screen2d",
        "//backends/jvm-shared:jvm-shared",
        "//backends/desktop-jvm-shared:desktop-jvm-shared",
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
        "//engines:sgl-screen2d",
        "//backends/jvm-shared:jvm-shared",
        "//backends/desktop-jvm-shared:desktop-jvm-shared",
        "@maven//:org_scalatest_scalatest_3",
    ],
    visibility=["//visibility:public"],
)
