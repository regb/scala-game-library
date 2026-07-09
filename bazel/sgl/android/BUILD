load("//bazel/sgl:assets.bzl", "sgl_assets_provider", "sgl_drawable_assets", "sgl_text_assets")
load("//bazel/sgl/android:android.bzl", "sgl_android_app")
load("@rules_java//java:java_test.bzl", "java_test")

exports_files([
    "AndroidManifest.xml.tpl",
    "AndroidRunner.sh.tpl",
    "MainActivity.kt.tpl",
    "app-build.gradle.kts.tpl",
    "gradle.properties.tpl",
    "proguard-rules.pro.tpl",
    "root-build.gradle.kts.tpl",
    "settings.gradle.kts.tpl",
], visibility = ["//visibility:public"])

sgl_drawable_assets(
    name = "typed-assets-test-drawable",
    density_srcs = {
        "mdpi": [
            "testdata/Player Icon.png",
            "testdata/sample.png",
        ],
        "xhdpi": ["testdata/xhdpi/sample.png"],
    },
    density_strip_prefixes = {
        "mdpi": "bazel/sgl/android/testdata",
        "xhdpi": "bazel/sgl/android/testdata/xhdpi",
    },
)

sgl_text_assets(
    name = "typed-assets-test-text",
    srcs = ["testdata/sample.txt"],
    strip_prefix = "bazel/sgl/android/testdata",
)

sgl_assets_provider(
    name = "typed-assets-test-provider",
    package = "sgl.bazel.android.test",
    asset_groups = [":typed-assets-test-drawable"],
)

sgl_android_app(
    name = "typed-assets-test",
    package = "sgl.bazel.android.test",
    label = "SGL Android typed asset test",
    assets = [
        ":typed-assets-test-text",
        ":typed-assets-test-drawable",
    ],
)

java_test(
    name = "typed_assets_manifest_test",
    srcs = ["AndroidTypedAssetsManifestTest.java"],
    main_class = "sgl.bazel.android.AndroidTypedAssetsManifestTest",
    use_testrunner = False,
    data = [
        ":typed-assets-test-debug",
        ":typed-assets-test-provider",
    ],
)
