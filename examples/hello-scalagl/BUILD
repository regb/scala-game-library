load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_raw_image_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

sgl_raw_image_assets(
    name = "images",
    srcs = [
        "assets/earth.png",
        "assets/moon.png",
        "assets/sun.png",
    ],
    strip_prefix = "examples/hello-scalagl/assets",
)

sgl_audio_assets(
    name = "audio",
    srcs = glob(["assets/*.wav"]),
    strip_prefix = "examples/hello-scalagl/assets",
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = [
        ":images",
        ":audio",
    ],
    package = "sgl.examples.hello.scalagl.core",
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    data = [
        ":audio",
        ":images",
    ],
    extra_mixins = [
        "sgl.examples.hello.scalagl.core.GeneratedAssetsProvider",
        "_root_.sgl.desktop.SaveScreenshotFromProperty",
    ],
    frame_height = 700,
    frame_width = 1000,
    main_class = "Main",
    package = "sgl.examples.hello.scalagl",
    window_title = "SGL Hello ScalaGL",
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "html5",
    assets = [
        ":images",
        ":audio",
    ],
    assets_strip_prefix = "examples/hello-scalagl/assets",
    canvas_height = 700,
    canvas_width = 1000,
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    export_name = "HelloScalaGL",
    extra_mixins = ["sgl.examples.hello.scalagl.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.examples.hello.scalagl",
    static_folder = "static",
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    assets = [
        ":images",
        ":audio",
    ],
    assets_strip_prefix = "examples/hello-scalagl/assets",
    backend_app = "NativeOpenGLApp",
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    extra_mixins = ["sgl.examples.hello.scalagl.core.GeneratedAssetsProvider"],
    frame_height = 700,
    frame_width = 1000,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGLESv2",
    ],
    main_class = "Main",
    package = "sgl.examples.hello.scalagl",
    use_screen2d = False,
    deps = [":core"],
)
