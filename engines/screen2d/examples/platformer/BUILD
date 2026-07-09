load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_awt_app", "sgl_desktop_native_app")
load("//bazel/sgl:assets.bzl", "sgl_drawable_assets")
load("//bazel/tiled:defs.bzl", "sgl_tiled_import")

package(default_visibility = ["//visibility:public"])

sgl_tiled_import(
    name = "tiled-assets",
    project_root = "engines/screen2d/examples/platformer/tiled",
    maps = glob(["tiled/maps/*.tmj"]),
    tilesets = glob(["tiled/tilesets/*.tsj"]),
    images = glob(["tiled/images/**/*.png"]),
    source_density = "mdpi",
    target_densities = ["mdpi"],
)

sgl_drawable_assets(
    name = "assets",
    srcs = glob(["assets/sprites/*.png"]),
    strip_prefix = "engines/screen2d/examples/platformer/assets/sprites",
    source_density = "mdpi",
    target_densities = ["mdpi"],
)

sgl_core_library(
    name = "core",
    include_default_modules = True,
    deps = ["//engines:sgl-screen2d"],
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    assets = [":assets", ":tiled-assets"],
    package = "sgl.examples.screen2d.platformer.core",
    use_extension_tiled = True,
)

sgl_desktop_awt_app(
    name = "desktop-awt",
    package = "sgl.examples.screen2d.platformer",
    main_class = "Main",
    core_abstract_class = "sgl.examples.screen2d.platformer.core.AbstractApp",
    deps = [":core"],
    frame_width = 800,
    frame_height = 800,
    use_extension_tiled = True,
    extra_mixins = ["sgl.examples.screen2d.platformer.core.GeneratedAssetsProvider"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.screen2d.platformer",
    main_class = "Main",
    core_abstract_class = "sgl.examples.screen2d.platformer.core.AbstractApp",
    deps = [":core"],
    assets = [":assets", ":tiled-assets"],
    assets_strip_prefix = "engines/screen2d/examples/platformer/assets",
    backend_app = "NativeApp",
    linkopts = ["-lSDL2", "-lSDL2_image", "-lGL"],
    frame_width = 600,
    frame_height = 800,
    screen_force_ppi = 160,
    use_extension_tiled = True,
    extra_mixins = ["sgl.examples.screen2d.platformer.core.GeneratedAssetsProvider"],
)
