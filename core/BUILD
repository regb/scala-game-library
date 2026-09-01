load(
    "@rules_scala//scala:scala.bzl",
    "scala_test",
)
load("//bazel:cross.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

exports_files(
    ["src/main/scala/sgl/PortableFontData.scala"],
    visibility = ["//tools/font-atlas-generator:__pkg__"],
)

scala_library(
    name = "sgl-core",
    srcs = glob(["src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
)

scala_library(
    name = "sgl-core-testlib",
    testonly = True,
    srcs = glob(["src/test/scala/sgl/util/*"]),
    visibility = ["//visibility:public"],
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_3",
        "@rules_scala//testing/toolchain:scalatest_classpath",
    ],
)

scala_test(
    name = "sgl-core-tests",
    srcs = glob(["src/test/scala/**/*.scala"]),
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        ":sgl-core",
        "//engines:sgl-screen2d",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
