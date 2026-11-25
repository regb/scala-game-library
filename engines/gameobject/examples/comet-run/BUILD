load("//bazel/sgl:assets.bzl", "sgl_raw_image_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

package(default_visibility = ["//visibility:public"])

sgl_raw_image_assets(
    name = "images",
    srcs = glob(["assets/*.png"]),
    strip_prefix = "engines/gameobject/examples/comet-run/assets",
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = [
        ":images",
    ],
    package = "sgl.examples.gameobject.comet.core",
    deps = ["//engines:sgl-gameobject"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.gameobject.comet.core.AbstractApp",
    data = [
        ":images",
    ],
    extra_mixins = ["sgl.examples.gameobject.comet.core.GeneratedAssetsProvider"],
    frame_height = 600,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.gameobject.comet",
    window_title = "Comet Run",
    deps = [
        ":core",
        "//engines:sgl-gameobject",
    ],
)

sgl_html5_opengl_app(
    name = "html5",
    assets = [
        ":images",
    ],
    assets_strip_prefix = "engines/gameobject/examples/comet-run/assets",
    canvas_height = 600,
    canvas_width = 800,
    core_abstract_class = "sgl.examples.gameobject.comet.core.AbstractApp",
    export_name = "CometRun",
    extra_mixins = ["sgl.examples.gameobject.comet.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.examples.gameobject.comet",
    static_folder = "static",
    deps = [
        ":core",
        "//engines:sgl-gameobject",
    ],
)

sgl_desktop_native_app(
    name = "desktop-native",
    assets = [
        ":images",
    ],
    assets_strip_prefix = "engines/gameobject/examples/comet-run/assets",
    backend_app = "NativeOpenGLApp",
    core_abstract_class = "sgl.examples.gameobject.comet.core.AbstractApp",
    extra_mixins = ["sgl.examples.gameobject.comet.core.GeneratedAssetsProvider"],
    frame_height = 600,
    frame_width = 800,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGLESv2",
    ],
    main_class = "Main",
    package = "sgl.examples.gameobject.comet",
    use_screen2d = False,
    deps = [
        ":core",
        "//engines:sgl-gameobject",
    ],
)
