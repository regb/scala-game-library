load("@rules_scala//scala:scala.bzl", "scala_library", "scala_binary")

load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_html5_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala", "MainScreen.scala"],
    resources = glob(["assets/**"]),
    resource_strip_prefix = "examples/hello/assets",
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "com.regblanc.sgl.hello",
    main_class = "Main",
    core_abstract_class = "com.regblanc.sgl.hello.core.AbstractApp",
    deps = [":core"],
    frame_width = 800,
    frame_height = 800,
)

sgl_html5_app(
    name = "html5",
    package = "com.regblanc.sgl.hello",
    main_class = "HelloGame",
    deps = [":core"],
    core_abstract_class = "com.regblanc.sgl.hello.core.AbstractApp",
    assets = glob(["assets/**"]),
    assets_strip_prefix = "examples/hello/assets",
    static_folder = "static",
    canvas_width = 800,
    canvas_height = 800,
)
