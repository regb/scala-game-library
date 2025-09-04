load(
    "@rules_scala//scala:scala.bzl",
    "scala_library", "scala_binary",
)
load("@rules_go//go:def.bzl", "go_binary")
load(":defs.bzl", "scalajs_html_template")

scalajs_html_template(
    name = "snake_html",
    scalajs_target = "//examples/snake:html5",
    main_class = "SnakeGame",
    output_name = "index.html",
)

go_binary(
    name = "server",
    srcs = ["server.go"],
    visibility = ["//visibility:public"],
)


scala_binary(
    name = "linker",
    srcs = ["ScalaJsLinker.scala"],
    deps = [
        "@maven//:org_scala_js_scalajs_ir_2_13",
        "@maven//:org_scala_js_scalajs_linker_2_13",
        "@maven//:org_scala_js_scalajs_linker_interface_2_13",
        "@maven//:org_scala_js_scalajs_logging_2_13",
    ],
    main_class = "ScalaJsLinker",
    visibility = ["//visibility:public"]
)

exports_files([
    "default.html.template",
], visibility = ["//visibility:public"])
