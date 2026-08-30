load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_raw_image_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

package(default_visibility = ["//visibility:public"])

sgl_raw_image_assets(
    name = "images",
    srcs = glob(["assets/*.png"]),
    strip_prefix = "engines/gameobject/examples/solar-system/assets",
)

sgl_audio_assets(
    name = "audio",
    srcs = glob(["assets/*.wav"]),
    strip_prefix = "engines/gameobject/examples/solar-system/assets",
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = [
        ":images",
        ":audio",
    ],
    package = "sgl.examples.gameobject.solar.core",
    deps = ["//engines:sgl-gameobject"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    data = [
        ":audio",
        ":images",
    ],
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
    frame_height = 700,
    frame_width = 1000,
    main_class = "Main",
    package = "sgl.examples.gameobject.solar",
    window_title = "SGL Solar System GO",
    deps = [
        ":core",
        "//engines:sgl-gameobject",
    ],
)

sgl_html5_opengl_app(
    name = "html5",
    assets = [
        ":images",
        ":audio",
    ],
    assets_strip_prefix = "engines/gameobject/examples/solar-system/assets",
    canvas_height = 700,
    canvas_width = 1000,
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    export_name = "SolarSystemGo",
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.examples.gameobject.solar",
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
        ":audio",
    ],
    assets_strip_prefix = "engines/gameobject/examples/solar-system/assets",
    backend_app = "NativeOpenGLApp",
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
    frame_height = 700,
    frame_width = 1000,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGLESv2",
    ],
    main_class = "Main",
    package = "sgl.examples.gameobject.solar",
    use_screen2d = False,
    deps = [
        ":core",
        "//engines:sgl-gameobject",
    ],
)
