load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    deps = ["//engines:sgl-screen2d"],
    srcs = glob(["core/src/main/scala/**/*.scala"]),
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "sgl.examples.screen2d.menu",
    main_class = "Main",
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    deps = [":core"],
    frame_width = 400,
    frame_height = 600,
)

sgl_html5_app(
    name = "html5",
    package = "sgl.examples.screen2d.menu",
    main_class = "MenuGame",
    deps = [":core"],
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    canvas_width = 400,
    canvas_height = 600,
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.screen2d.menu",
    main_class = "Main",
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    deps = [":core"],
    frame_width = 400,
    frame_height = 600,
)
