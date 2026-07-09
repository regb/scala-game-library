load("//bazel/sgl:assets.bzl", "sgl_assets_provider", "sgl_text_assets")
load("@rules_java//java:java_test.bzl", "java_test")

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

sgl_text_assets(
    name = "keyword_text_asset",
    srcs = ["testdata/type.txt"],
    strip_prefix = "bazel/sgl/testdata",
)

sgl_assets_provider(
    name = "keyword_assets_provider",
    package = "sgl.assets.test",
    asset_groups = [":keyword_text_asset"],
)

java_test(
    name = "assets_provider_test",
    srcs = ["AssetsProviderTest.java"],
    args = ["$(location :keyword_assets_provider)"],
    data = [":keyword_assets_provider"],
    main_class = "AssetsProviderTest",
    use_testrunner = False,
)
