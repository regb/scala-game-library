load("//bazel:cross.bzl", "scala_library")
load("//bazel/sgl:defs.bzl", "sgl_desktop_awt_app", "sgl_html5_app")

scala_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "com.regblanc.sgl.snake",
    main_class = "Main",
    core_abstract_class = "com.regblanc.sgl.snake.core.AbstractApp",
    deps = [
        "//examples/snake:core",
        "//jvm-shared:jvm-shared",
    ],
)

sgl_html5_app(
    name = "html5",
    package = "com.regblanc.sgl.snake",
    main_class = "SnakeGame",
    deps = [
        ":core",
    ],
    core_abstract_class = "com.regblanc.sgl.snake.core.AbstractApp",
)                                                            
