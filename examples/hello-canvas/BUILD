load("//bazel/sgl:assets.bzl", "sgl_audio_assets", "sgl_drawable_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_app", "sgl_html5_opengl_app")

sgl_drawable_assets(
    name = "drawables",
    srcs = ["assets/drawable-xhdpi/character.png"],
    source_density = "xhdpi",
    strip_prefix = "examples/hello-canvas/assets/drawable-xhdpi",
    target_densities = [
        "mdpi",
        "xhdpi",
    ],
)

sgl_audio_assets(
    name = "audio",
    srcs = glob(["assets/audio/*.wav"]),
    strip_prefix = "examples/hello-canvas/assets/audio",
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = [
        ":drawables",
        ":audio",
    ],
    include_default_modules = False,
    package = "sgl.examples.hello.canvas.core",
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    extra_mixins = [
        "sgl.examples.hello.canvas.core.GeneratedAssetsProvider",
        "_root_.sgl.desktop.SaveScreenshotFromProperty",
    ],
    frame_height = 800,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.hello.canvas",
    use_screen2d = False,
    deps = [":core"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    data = [
        ":audio",
        ":drawables",
    ],
    extra_mixins = [
        "sgl.examples.hello.canvas.core.GeneratedAssetsProvider",
        "_root_.sgl.OpenGLCanvasProvider",
    ],
    frame_height = 800,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.hello.canvas",
    deps = [":core"],
)

sgl_html5_app(
    name = "html5",
    assets = [
        ":drawables",
        ":audio",
    ],
    assets_strip_prefix = "examples/hello-canvas/assets",
    canvas_height = 800,
    canvas_width = 800,
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    extra_mixins = ["sgl.examples.hello.canvas.core.GeneratedAssetsProvider"],
    main_class = "HelloGame",
    package = "sgl.examples.hello.canvas",
    static_folder = "static",
    use_screen2d = False,
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "html5-opengl",
    assets = [
        ":drawables",
        ":audio",
    ],
    assets_strip_prefix = "examples/hello-canvas/assets",
    canvas_height = 800,
    canvas_width = 800,
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    export_name = "HelloOpenGLGame",
    extra_mixins = [
        "sgl.examples.hello.canvas.core.GeneratedAssetsProvider",
        "_root_.sgl.OpenGLCanvasProvider",
    ],
    html_output_name = "opengl-index.html",
    main_class = "Main",
    output_name = "opengl-index.js",
    package = "sgl.examples.hello.canvas",
    static_folder = "static",
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    assets = [
        ":drawables",
        ":audio",
    ],
    assets_strip_prefix = "examples/hello-canvas/assets",
    core_abstract_class = "sgl.examples.hello.canvas.core.AbstractApp",
    extra_mixins = ["sgl.examples.hello.canvas.core.GeneratedAssetsProvider"],
    frame_height = 800,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.hello.canvas",
    use_screen2d = False,
    deps = [":core"],
)
