load("@rules_scala//scala:scala.bzl", "setup_scala_toolchain")
load(
    "@rules_scala//scala:scala.bzl",
    "scala_library", "scala_binary",
)
load("//bazel:scalajs.bzl", "scalajs_link")

scala_library(
    name = "Test.js",
    srcs = [ "Test.scala" ],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
    deps = [ "@maven//:org_scala_js_scalajs_library_2_13" ],
)

scalajs_link(
    name = "index",
    scala_libs = [":Test.js"],
    linker = "//bazel/scalajs:linker",
    main_class = "Test",
    output_name = "index.js",
)
