load("//bazel/sgl:assets.bzl", "sgl_drawable_assets", "sgl_texture_atlas")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

sgl_drawable_assets(
    name = "sprites",
    srcs = glob(["assets/sprites/*.png"]),
    source_density = "mdpi",
    strip_prefix = "examples/hello-atlas/assets/sprites",
    target_densities = ["mdpi"],
)

# Packs the sprites into one page. Consumers use this target instead of :sprites.
sgl_texture_atlas(
    name = "sprite_atlas",
    assets = [":sprites"],
    max_texture_size = 512,
)

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = [":sprite_atlas"],
    package = "sgl.examples.hello.atlas.core",
    deps = ["//engines:sgl-gameobject"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.hello.atlas.core.AbstractApp",
    data = [":sprite_atlas"],
    extra_mixins = ["sgl.examples.hello.atlas.core.GeneratedAssetsProvider"],
    frame_height = 400,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.hello.atlas",
    window_title = "SGL Hello Atlas",
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "html5",
    assets = [":sprite_atlas"],
    assets_strip_prefix = "examples/hello-atlas/assets",
    canvas_height = 400,
    canvas_width = 800,
    core_abstract_class = "sgl.examples.hello.atlas.core.AbstractApp",
    export_name = "HelloAtlas",
    extra_mixins = ["sgl.examples.hello.atlas.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.examples.hello.atlas",
    static_folder = "static",
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    assets = [":sprite_atlas"],
    assets_strip_prefix = "examples/hello-atlas/assets",
    backend_app = "NativeOpenGLApp",
    core_abstract_class = "sgl.examples.hello.atlas.core.AbstractApp",
    extra_mixins = ["sgl.examples.hello.atlas.core.GeneratedAssetsProvider"],
    frame_height = 400,
    frame_width = 800,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGLESv2",
    ],
    main_class = "Main",
    package = "sgl.examples.hello.atlas",
    use_screen2d = False,
    deps = [":core"],
)
