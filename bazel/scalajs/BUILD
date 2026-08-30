load("@rules_go//go:def.bzl", "go_binary")
load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

go_binary(
    name = "server",
    srcs = ["server.go"],
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "linker",
    srcs = ["ScalaJsLinker.scala"],
    main_class = "ScalaJsLinker",
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_scala_js_scalajs_ir_2_13",
        "@maven//:org_scala_js_scalajs_linker_2_13",
        "@maven//:org_scala_js_scalajs_linker_interface_2_13",
        "@maven//:org_scala_js_scalajs_logging_2_13",
    ],
)

exports_files(
    [
        "default.html.template",
    ],
    visibility = ["//visibility:public"],
)
