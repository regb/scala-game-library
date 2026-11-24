load("@rules_scala//scala:scala.bzl", "scala_binary")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel/sgl:assets.bzl", "sgl_drawable_assets", "sgl_raw_image_assets", "sgl_texture_atlas")
load("//bazel/sgl:defs.bzl", "sgl_core_library")

sgl_drawable_assets(
    name = "sprites",
    srcs = glob(["assets/sprites/*.png"]),
    source_density = "mdpi",
    strip_prefix = "engines/gameobject/benchmarks/assets/sprites",
    target_densities = ["mdpi"],
)

sgl_texture_atlas(
    name = "sprite_atlas",
    assets = [":sprites"],
    max_texture_size = 512,
)

sgl_raw_image_assets(
    name = "images",
    srcs = ["assets/heart.png"],
    strip_prefix = "engines/gameobject/benchmarks/assets",
)

sgl_core_library(
    name = "core",
    srcs = ["BenchmarkApp.scala"],
    assets = [
        ":images",
        ":sprite_atlas",
    ],
    package = "sgl.benchmarks.gameobject.core",
    deps = ["//engines:sgl-gameobject"],
)

# See README.md.
scala_binary(
    name = "benchmarks",
    srcs = ["Benchmarks.scala"],
    main_class = "sgl.benchmarks.gameobject.Benchmarks",
    scalacopts = SGL_SCALACOPTS,
    target_compatible_with = ["@platforms//os:linux"],
    deps = [
        ":core",
        "//backends/desktop-jvm-shared",
        "//backends/headless-opengl:sgl-headless-opengl",
        "//backends/lwjgl-shared:sgl-lwjgl-shared",
        "//core:sgl-core",
        "//engines:sgl-gameobject",
        "@maven//:org_lwjgl_lwjgl_opengl",
    ],
)
