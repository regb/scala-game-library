load("@rules_scala//scala:scala.bzl", "scala_binary")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_binary(
    name = "linker",
    srcs = ["ScalaNativeLinker.scala"],
    main_class = "ScalaNativeLinker",
    scalacopts = SGL_SCALACOPTS,
    deps = [
        "@maven//:org_scala_native_tools_3",
        "@maven//:org_scala_native_util_3",
    ],
    visibility = ["//visibility:public"],
)
