load("@rules_scala//scala:scala.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

exports_files(["metal_bridge.m"])

scala_library(
    name = "sgl-metal-native",
    srcs = glob(["src/main/scala/**/*.scala"]),
    plugins = ["@maven//:org_scala_native_nscplugin_3_3_8"],
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
        "@maven//:org_scala_native_auxlib_native0_5_3",
        "@maven//:org_scala_native_clib_native0_5_3",
        "@maven//:org_scala_native_javalib_native0_5_3",
        "@maven//:org_scala_native_nativelib_native0_5_3",
        "@maven//:org_scala_native_posixlib_native0_5_3",
        "@maven//:org_scala_native_scalalib_native0_5_3",
    ],
)
