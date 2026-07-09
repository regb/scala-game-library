load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app", "sgl_html5_app")
load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_drawable_assets")

sgl_drawable_assets(
    name = "drawables",
    srcs = ["assets/drawable-xhdpi/character.png"],
    strip_prefix = "examples/hello-canvas/assets/drawable-xhdpi",
    source_density = "xhdpi",
    target_densities = ["mdpi", "xhdpi"],
)

sgl_audio_assets(
    name = "audio",
    srcs = glob(["assets/audio/*.wav"]),
    strip_prefix = "examples/hello-canvas/assets/audio",
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    include_default_modules = False,
    assets = [":drawables", ":audio"],
    package = "sgl.examples.hello.canvas.core",
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "sgl.examples.hello.canvas",
    main_class = "Main",
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    deps = [":core"],
    use_screen2d = False,
    frame_width = 800,
    frame_height = 800,
    extra_mixins = ["sgl.examples.hello.canvas.core.GeneratedAssetsProvider"],
)

sgl_html5_app(
    name = "html5",
    package = "sgl.examples.hello.canvas",
    main_class = "HelloGame",
    deps = [":core"],
    use_screen2d = False,
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    assets = [":drawables", ":audio"],
    assets_strip_prefix = "examples/hello-canvas/assets",
    static_folder = "static",
    canvas_width = 800,
    canvas_height = 800,
    extra_mixins = ["sgl.examples.hello.canvas.core.GeneratedAssetsProvider"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.hello.canvas",
    main_class = "Main",
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    deps = [":core"],
    use_screen2d = False,
    assets = [":drawables", ":audio"],
    assets_strip_prefix = "examples/hello-canvas/assets",
    frame_width = 800,
    frame_height = 800,
    extra_mixins = ["sgl.examples.hello.canvas.core.GeneratedAssetsProvider"],
)
