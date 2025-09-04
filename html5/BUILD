load("//bazel:scalajs.bzl", "scalajs_library")

scalajs_library(
    name="sgl-html5",
    srcs=glob(["src/main/**/*.scala"]),
    deps=[
        "//core:sgl-core",
        "@maven//:org_scala_js_scalajs_dom_sjs1_2_13",
    ],

    visibility=["//visibility:public"],
)
