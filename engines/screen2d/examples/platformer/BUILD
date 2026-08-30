load("//bazel/sgl:assets.bzl", "sgl_drawable_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app")
load("//bazel/tiled:defs.bzl", "sgl_tiled_import")

package(default_visibility = ["//visibility:public"])

sgl_tiled_import(
    name = "tiled-assets",
    images = glob(["tiled/images/**/*.png"]),
    maps = glob(["tiled/maps/*.tmj"]),
    project_root = "engines/screen2d/examples/platformer/tiled",
    source_density = "mdpi",
    target_densities = ["mdpi"],
    tilesets = glob(["tiled/tilesets/*.tsj"]),
)

sgl_drawable_assets(
    name = "assets",
    srcs = glob(["assets/sprites/*.png"]),
    source_density = "mdpi",
    strip_prefix = "engines/screen2d/examples/platformer/assets/sprites",
    target_densities = ["mdpi"],
)

sgl_core_library(
    name = "core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    assets = [
        ":assets",
        ":tiled-assets",
    ],
    include_default_modules = True,
    package = "sgl.examples.screen2d.platformer.core",
    use_extension_tiled = True,
    deps = ["//engines:sgl-screen2d"],
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    core_abstract_class = "sgl.examples.screen2d.platformer.core.AbstractApp",
    extra_mixins = ["sgl.examples.screen2d.platformer.core.GeneratedAssetsProvider"],
    frame_height = 800,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.screen2d.platformer",
    use_extension_tiled = True,
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    assets = [
        ":assets",
        ":tiled-assets",
    ],
    assets_strip_prefix = "engines/screen2d/examples/platformer/assets",
    backend_app = "NativeApp",
    core_abstract_class = "sgl.examples.screen2d.platformer.core.AbstractApp",
    extra_mixins = ["sgl.examples.screen2d.platformer.core.GeneratedAssetsProvider"],
    frame_height = 800,
    frame_width = 600,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGL",
    ],
    main_class = "Main",
    package = "sgl.examples.screen2d.platformer",
    screen_force_ppi = 160,
    use_extension_tiled = True,
    deps = [":core"],
)
