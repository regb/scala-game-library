load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "com.regblanc.sgl.board",
    main_class = "Main",
    core_abstract_class = "com.regblanc.sgl.board.core.AbstractApp",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    package = "com.regblanc.sgl.board",
    main_class = "BoardGame",
    deps = [":core"],
    core_abstract_class = "com.regblanc.sgl.board.core.AbstractApp",
)
