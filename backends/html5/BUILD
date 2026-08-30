load("//bazel:scalajs.bzl", "scalajs_library")

scalajs_library(
    name = "sgl-html5",
    srcs = glob(["src/main/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
        "//engines:sgl-screen2d",
        "@maven//:org_scala_js_scalajs_dom_sjs1_3",
    ],
)
