load("//backends/html5/browser-testing:defs.bzl", "html5_browser_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel/sgl:assets.bzl", "sgl_audio_assets")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_html5_app")

sgl_audio_assets(
    name = "audio",
    srcs = glob(["assets/audio/*.wav"]),
    strip_prefix = "backends/html5/browser-testing/audio/assets/audio",
)

sgl_core_library(
    name = "core",
    srcs = ["Game.scala"],
    assets = [":audio"],
    package = "sgl.html5.testing.audio.core",
    deps = [],
)

sgl_html5_app(
    name = "game",
    assets = [":audio"],
    assets_strip_prefix = "backends/html5/browser-testing/audio/assets",
    canvas_height = 100,
    canvas_width = 100,
    center_canvas = False,
    core_abstract_class = "sgl.html5.testing.audio.core.Game",
    extra_mixins = ["sgl.html5.testing.audio.core.GeneratedAssetsProvider"],
    main_class = "Main",
    package = "sgl.html5.testing.audio",
    use_screen2d = False,
    deps = [":core"],
)

html5_browser_test(
    name = "audio-test",
    size = "medium",
    srcs = ["AudioTest.scala"],
    scalacopts = SGL_SCALACOPTS,
    sites = [":game_site"],
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
)
