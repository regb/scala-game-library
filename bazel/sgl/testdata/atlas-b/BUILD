load("//bazel/sgl:assets.bzl", "sgl_drawable_assets", "sgl_texture_atlas")

sgl_drawable_assets(
    name = "sprites",
    srcs = ["//bazel/sgl:testdata/atlas/gem.png"],
    source_density = "mdpi",
    strip_prefix = "bazel/sgl/testdata/atlas",
    target_densities = ["mdpi"],
)

sgl_texture_atlas(
    name = "shared",
    assets = [":sprites"],
    max_texture_size = 64,
    visibility = ["//bazel/sgl:__pkg__"],
)
