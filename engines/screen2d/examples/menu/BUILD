load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    include_default_modules = True,
    deps = ["//engines:sgl-screen2d"],
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    frame_height = 600,
    frame_width = 400,
    main_class = "Main",
    package = "sgl.examples.screen2d.menu",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    canvas_height = 600,
    canvas_width = 400,
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    main_class = "MenuGame",
    package = "sgl.examples.screen2d.menu",
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    core_abstract_class = "sgl.examples.screen2d.menu.core.AbstractApp",
    frame_height = 600,
    frame_width = 400,
    main_class = "Main",
    package = "sgl.examples.screen2d.menu",
    deps = [":core"],
)
