load("@rules_java//java:java_test.bzl", "java_test")
load("//bazel/sgl:assets.bzl", "sgl_assets_provider", "sgl_drawable_assets", "sgl_text_assets", "sgl_texture_atlas")
load("//bazel/sgl:core.bzl", "sgl_core_library")

exports_files(
    [
        "Html5Main.scala.template",
        "Html5OpenGLMain.scala.template",
        "DesktopAWTMain.scala.template",
        "DesktopLwjglMain.scala.template",
        "DesktopNativeMain.scala.template",
    ],
    visibility = ["//visibility:private"],
)

exports_files([
    "testdata/atlas/coin.png",
    "testdata/atlas/gem.png",
])

sgl_text_assets(
    name = "keyword_text_asset",
    srcs = ["testdata/type.txt"],
    strip_prefix = "bazel/sgl/testdata",
)

sgl_assets_provider(
    name = "keyword_assets_provider",
    asset_groups = [":keyword_text_asset"],
    package = "sgl.assets.test",
)

java_test(
    name = "assets_provider_test",
    srcs = ["AssetsProviderTest.java"],
    args = ["$(location :keyword_assets_provider)"],
    data = [":keyword_assets_provider"],
    main_class = "AssetsProviderTest",
    use_testrunner = False,
)

sgl_drawable_assets(
    name = "atlas_test_sprites",
    srcs = glob(["testdata/atlas/*.png"]),
    source_density = "xhdpi",
    strip_prefix = "bazel/sgl/testdata/atlas",
    target_densities = [
        "mdpi",
        "xhdpi",
    ],
)

sgl_texture_atlas(
    name = "atlas_test_atlas",
    assets = [
        ":atlas_test_sprites",
        ":keyword_text_asset",
    ],
    exclude = ["banner"],
    max_pages = 2,
    max_texture_size = 64,
)

sgl_assets_provider(
    name = "atlas_assets_provider",
    asset_groups = [":atlas_test_atlas"],
    package = "sgl.assets.atlastest",
)

sgl_assets_provider(
    name = "collision_assets_provider",
    asset_groups = [
        "//bazel/sgl/testdata/atlas-a:shared",
        "//bazel/sgl/testdata/atlas-b:shared",
    ],
    package = "sgl.assets.collisiontest",
)

java_test(
    name = "texture_atlas_namespace_test",
    srcs = ["TextureAtlasNamespaceTest.java"],
    args = [
        "$(location :collision_assets_provider)",
        "$(locations //bazel/sgl/testdata/atlas-a:shared)",
        "$(locations //bazel/sgl/testdata/atlas-b:shared)",
    ],
    data = [
        ":collision_assets_provider",
        "//bazel/sgl/testdata/atlas-a:shared",
        "//bazel/sgl/testdata/atlas-b:shared",
    ],
    main_class = "TextureAtlasNamespaceTest",
    use_testrunner = False,
)

# Compiles the generated atlas-aware Assets.scala, including the keyword-named text asset, against core.
sgl_core_library(
    name = "atlas_test_core",
    srcs = ["AtlasTestApp.scala"],
    assets = [":atlas_test_atlas"],
    package = "sgl.assets.atlastest",
)

java_test(
    name = "texture_atlas_rule_test",
    srcs = ["TextureAtlasRuleTest.java"],
    args = [
        "$(location :atlas_assets_provider)",
        "$(locations :atlas_test_atlas)",
    ],
    data = [
        ":atlas_assets_provider",
        ":atlas_test_atlas",
    ],
    main_class = "TextureAtlasRuleTest",
    use_testrunner = False,
)
