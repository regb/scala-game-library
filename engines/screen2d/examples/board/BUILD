load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    include_default_modules = True,
    deps = ["//engines:sgl-screen2d"],
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    core_abstract_class = "sgl.examples.screen2d.board.core.AbstractApp",
    main_class = "Main",
    package = "sgl.examples.screen2d.board",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    core_abstract_class = "sgl.examples.screen2d.board.core.AbstractApp",
    main_class = "BoardGame",
    package = "sgl.examples.screen2d.board",
    deps = [":core"],
)
