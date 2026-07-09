load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    deps = ["//engines:sgl-screen2d"],
    srcs = glob(["core/src/main/scala/**/*.scala"]),
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "sgl.examples.screen2d.board",
    main_class = "Main",
    core_abstract_class = "sgl.examples.screen2d.board.core.AbstractApp",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    package = "sgl.examples.screen2d.board",
    main_class = "BoardGame",
    deps = [":core"],
    core_abstract_class = "sgl.examples.screen2d.board.core.AbstractApp",
)
