load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")
load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_raw_image_assets")

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
    assets = [":images", ":audio"],
    package = "sgl.examples.hello.scalagl.core",
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    package = "sgl.examples.hello.scalagl",
    main_class = "Main",
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    deps = [":core"],
    data = [":images", ":audio"],
    frame_width = 1000,
    frame_height = 700,
    window_title = "SGL Hello ScalaGL",
    extra_mixins = ["sgl.examples.hello.scalagl.core.GeneratedAssetsProvider"],
)

sgl_html5_opengl_app(
    name = "html5",
    package = "sgl.examples.hello.scalagl",
    main_class = "Main",
    export_name = "HelloScalaGL",
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    deps = [":core"],
    assets = [":images", ":audio"],
    assets_strip_prefix = "examples/hello-scalagl/assets",
    static_folder = "static",
    canvas_width = 1000,
    canvas_height = 700,
    extra_mixins = ["sgl.examples.hello.scalagl.core.GeneratedAssetsProvider"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.hello.scalagl",
    main_class = "Main",
    core_abstract_class = "sgl.examples.hello.scalagl.core.AbstractApp",
    deps = [":core"],
    assets = [":images", ":audio"],
    assets_strip_prefix = "examples/hello-scalagl/assets",
    backend_app = "NativeOpenGLApp",
    use_screen2d = False,
    linkopts = ["-lSDL2", "-lSDL2_image", "-lGLESv2"],
    frame_width = 1000,
    frame_height = 700,
    extra_mixins = ["sgl.examples.hello.scalagl.core.GeneratedAssetsProvider"],
)
