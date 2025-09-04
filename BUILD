load("//bazel:scalajs.bzl", "scalajs_link")
load("//bazel:cross.bzl", "scala_library")

scala_library(
    name = "Test.js",
    srcs = [ "Test.scala" ],
)

scalajs_link(
    name = "index",
    scala_libs = [":Test.js"],
    linker = "//bazel/scalajs:linker",
    main_class = "Test",
    output_name = "index.js",
)
