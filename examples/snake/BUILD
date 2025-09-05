load("@rules_scala//scala:scala.bzl", "scala_binary")
load("//bazel:cross.bzl", "scala_library")
load("//bazel:scalajs.bzl", "scalajs_module")
load("//bazel/scalajs:defs.bzl", "run_server")
load("//bazel/sgl:html5.bzl", "html5_main")

scala_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "desktop-awt",
    srcs = glob(["desktop-awt/src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.snake.desktop.Main",
    deps = [
        "//examples/snake:core",
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "//desktop-awt:sgl-desktop-awt",
    ],
)

scalajs_module(
    name = "html5",
    srcs = [":Html5Main"],
    deps = [
        ":core",
        "//core:sgl-core",
        "//html5:sgl-html5",
        "@maven//:org_scala_js_scalajs_dom_sjs1_2_13",
    ],
    output_name = "index.js",
    visibility = ["//visibility:public"],
)

html5_main(
    name = "Html5Main",
    package = "com.regblanc.sgl.snake",
    main_class = "SnakeGame",
    core_abstract_class = "com.regblanc.sgl.snake.core.AbstractApp",
)

run_server(
    name = "serve_snake",
    scalajs_module = ":html5",
    main_class = "SnakeGame",
)
