load("@rules_scala//scala:scala.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

filegroup(
    name = "srcs",
    srcs = glob(["src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
)

scala_library(
    name = "sgl-desktop-native",
    srcs = [":srcs"],
    scalacopts = SGL_SCALACOPTS,
    plugins = ["@maven//:org_scala_native_nscplugin_3_3_8"],
    deps = [
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "@maven//:org_scala_native_scalalib_native0_5_3",
        "@maven//:org_scala_native_scala3lib_native0_5_3",
        "@maven//:org_scala_native_nativelib_native0_5_3",
        "@maven//:org_scala_native_clib_native0_5_3",
        "@maven//:org_scala_native_posixlib_native0_5_3",
        "@maven//:org_scala_native_javalib_native0_5_3",
        "@maven//:org_scala_native_auxlib_native0_5_3",
    ],
    visibility = ["//visibility:public"],
)
