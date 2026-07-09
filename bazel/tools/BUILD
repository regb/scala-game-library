load("@rules_java//java:java_binary.bzl", "java_binary")
load("@rules_java//java:java_test.bzl", "java_test")

java_binary(
    name = "image_asset_tool",
    srcs = ["ImageAssetTool.java"],
    main_class = "sgl.bazel.tools.ImageAssetTool",
    visibility = ["//visibility:public"],
)

java_test(
    name = "image_asset_tool_test",
    srcs = ["ImageAssetTool.java", "ImageAssetToolTest.java"],
    main_class = "sgl.bazel.tools.ImageAssetToolTest",
    use_testrunner = False,
)
