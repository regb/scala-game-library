load("@rules_scala//scala:scala.bzl", "scala_library", "scala_binary")
load("//bazel:scalajs.bzl", "scalajs_link")

scala_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
        "@maven//:org_scala_js_scalajs_library_2_13",
    ],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
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

scala_library(
    name = "html5",
    srcs = glob(["html5/src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.snake.html5.Main",
    deps = [
        ":core",
        "//core:sgl-core",
        "//html5:sgl-html5",
        "@maven//:org_scala_js_scalajs_library_2_13",
        "@maven//:org_scala_js_scalajs_dom_sjs1_2_13",
    ],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
)

scalajs_link(
    name = "index",
    scala_libs = [
        ":html5",
    ],
    linker = "//bazel/scalajs:linker",
    output_name = "index.js",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "html_files",
    srcs = ["html5/index.html"],
    visibility = ["//visibility:public"],
)
