load("@rules_java//java:java_test.bzl", "java_test")

java_test(
    name = "portable_font_data_test",
    srcs = [
        "GeneratePortableFontAtlas.java",
        "VerifyPortableFontAtlas.java",
    ],
    args = [
        "$(location //third_party/fonts/noto-sans:NotoSans-Regular.ttf)",
        "$(location //third_party/fonts/noto-sans:NotoSans-Bold.ttf)",
        "$(location //core:src/main/scala/sgl/PortableFontData.scala)",
    ],
    data = [
        "//core:src/main/scala/sgl/PortableFontData.scala",
        "//third_party/fonts/noto-sans:NotoSans-Bold.ttf",
        "//third_party/fonts/noto-sans:NotoSans-Regular.ttf",
    ],
    main_class = "VerifyPortableFontAtlas",
    use_testrunner = False,
)
