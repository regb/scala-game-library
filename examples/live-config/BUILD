load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel/sgl:assets.bzl", "sgl_raw_image_assets", "sgl_text_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_html5_opengl_app")

# Assets sit in the folders their typed paths use (raw-image/, text/), so a
# development build can read them straight from assets/ while it runs.
sgl_raw_image_assets(
    name = "images",
    srcs = glob(["assets/raw-image/*.png"]),
    strip_prefix = "examples/live-config/assets/raw-image",
)

sgl_text_assets(
    name = "texts",
    srcs = glob(["assets/text/*"]),
    strip_prefix = "examples/live-config/assets/text",
)

ASSETS = [
    ":images",
    ":texts",
]

# The game, shared by release and development builds.
sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    assets = ASSETS,
    package = "sgl.examples.liveconfig.core",
    deps = [
        "//engines:sgl-gameobject",
        "//modules/live-config",
    ],
)

# Development: the embedding page can call reloadAssets, see doc/hot-reload.md.
sgl_html5_opengl_app(
    name = "html5-dev",
    assets = ASSETS,
    assets_strip_prefix = "examples/live-config/assets",
    canvas_height = 400,
    canvas_width = 640,
    core_abstract_class = "sgl.examples.liveconfig.core.AbstractApp",
    export_name = "LiveConfigGame",
    extra_mixins = ["sgl.examples.liveconfig.core.GeneratedAssetsProvider"],
    hot_reload = True,
    html_output_name = "index-dev.html",
    main_class = "Main",
    output_name = "index-dev.js",
    package = "sgl.examples.liveconfig.dev",
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "html5",
    assets = ASSETS,
    assets_strip_prefix = "examples/live-config/assets",
    canvas_height = 400,
    canvas_width = 640,
    core_abstract_class = "sgl.examples.liveconfig.core.AbstractApp",
    export_name = "LiveConfigGame",
    extra_mixins = ["sgl.examples.liveconfig.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.examples.liveconfig.release",
    deps = [":core"],
)

# Development: reads assets from SGL_ASSET_DIRECTORY, and reloads the files
# that change there.
sgl_desktop_lwjgl_app(
    name = "desktop-dev",
    core_abstract_class = "sgl.examples.liveconfig.core.AbstractApp",
    extra_mixins = [
        "sgl.examples.liveconfig.core.GeneratedAssetsProvider",
        "sgl.desktop.DesktopAssetDirectory",
    ],
    frame_height = 400,
    frame_width = 640,
    main_class = "Main",
    package = "sgl.examples.liveconfig.dev",
    window_title = "SGL hot reload",
    deps = [":core"],
)

sgl_desktop_lwjgl_app(
    name = "desktop",
    core_abstract_class = "sgl.examples.liveconfig.core.AbstractApp",
    extra_mixins = ["sgl.examples.liveconfig.core.GeneratedAssetsProvider"],
    frame_height = 400,
    frame_width = 640,
    main_class = "Main",
    package = "sgl.examples.liveconfig.release",
    window_title = "SGL hot reload",
    deps = [":core"],
)

scala_test(
    name = "headless-test",
    srcs = ["HotReloadHeadlessSuite.scala"],
    env = {
        "DISPLAY": "",
        "EGL_PLATFORM": "surfaceless",
        "LIBGL_ALWAYS_SOFTWARE": "1",
        "WAYLAND_DISPLAY": "",
    },
    scalacopts = SGL_SCALACOPTS,
    target_compatible_with = ["@platforms//os:linux"],
    deps = [
        ":core",
        "//backends/desktop-jvm-shared",
        "//backends/headless-opengl:sgl-headless-opengl",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "//engines:sgl-gameobject",
        "//modules/live-config",
        "@maven//:org_scalatest_scalatest_3",
    ],
)
