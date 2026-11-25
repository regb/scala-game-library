load("//backends/html5/browser-testing:defs.bzl", "html5_browser_test")
load("//bazel/sgl:assets.bzl", "sgl_raw_image_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_html5_opengl_app")

sgl_raw_image_assets(
    name = "images",
    srcs = glob(["assets/*.png"]),
    strip_prefix = "backends/html5/browser-testing/webgl/assets",
)

sgl_core_library(
    name = "core",
    srcs = ["Game.scala"],
    assets = [":images"],
    package = "sgl.html5.testing.webgl.core",
    deps = ["//engines:sgl-gameobject"],
)

sgl_html5_opengl_app(
    name = "game",
    assets = [":images"],
    assets_strip_prefix = "backends/html5/browser-testing/webgl/assets",
    canvas_height = 100,
    canvas_width = 300,
    center_canvas = False,
    core_abstract_class = "sgl.html5.testing.webgl.core.Game",
    export_name = "Game",
    extra_mixins = ["sgl.html5.testing.webgl.core.GeneratedAssetsProvider"],
    hot_reload = True,
    html_template = "page.html",
    main_class = "Main",
    pack_assets = True,
    package = "sgl.html5.testing.webgl",
    deps = [":core"],
)

html5_browser_test(
    name = "webgl-test",
    size = "medium",
    srcs = ["WebGLTest.scala"],
    sites = [":game_site"],
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
)
