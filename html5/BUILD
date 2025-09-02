load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_library",
    "scala_test",
)

scala_library(
    name="sgl-html5",
    srcs=glob(["src/main/**/*.scala"]),
    deps=[
        "//core:sgl-core",
        "@maven//:org_scala_js_scalajs_library_2_13",
        "@maven//:org_scala_js_scalajs_dom_sjs1_2_13",
    ],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
    visibility=["//visibility:public"],
)
