load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")
load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_raw_image_assets")

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
    assets = [":images", ":audio"],
    package = "sgl.examples.gameobject.solar.core",
    deps = ["//engines:sgl-gameobject"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    package = "sgl.examples.gameobject.solar",
    main_class = "Main",
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    deps = [":core", "//engines:sgl-gameobject"],
    data = [":images", ":audio"],
    frame_width = 1000,
    frame_height = 700,
    window_title = "SGL Solar System GO",
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
)

sgl_html5_opengl_app(
    name = "html5",
    package = "sgl.examples.gameobject.solar",
    main_class = "Main",
    export_name = "SolarSystemGo",
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    deps = [":core", "//engines:sgl-gameobject"],
    assets = [":images", ":audio"],
    assets_strip_prefix = "engines/gameobject/examples/solar-system/assets",
    static_folder = "static",
    canvas_width = 1000,
    canvas_height = 700,
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.gameobject.solar",
    main_class = "Main",
    core_abstract_class = "sgl.examples.gameobject.solar.core.AbstractApp",
    deps = [":core", "//engines:sgl-gameobject"],
    assets = [":images", ":audio"],
    assets_strip_prefix = "engines/gameobject/examples/solar-system/assets",
    backend_app = "NativeOpenGLApp",
    use_screen2d = False,
    linkopts = ["-lSDL2", "-lSDL2_image", "-lGLESv2"],
    frame_width = 1000,
    frame_height = 700,
    extra_mixins = ["sgl.examples.gameobject.solar.core.GeneratedAssetsProvider"],
)
