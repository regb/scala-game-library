load("@rules_scala//scala:scala.bzl", "scala_binary")
load(":defs.bzl", "binary_test")

# The checked-in portable font data is compared byte for byte with what the
# generator produces, and glyph rasterization differs between JDK releases
# (JDK 25 no longer reproduces it). The generator therefore runs on a pinned
# runtime instead of whichever JDK the machine provides.
scala_binary(
    name = "verify_portable_font_atlas",
    srcs = [
        "GeneratePortableFontAtlas.java",
        "VerifyPortableFontAtlas.java",
    ],
    main_class = "VerifyPortableFontAtlas",
    runtime_jdk = "@rules_java//toolchains:remotejdk_17",
)

binary_test(
    name = "portable_font_data_test",
    args = [
        "$(rootpath //third_party/fonts/noto-sans:NotoSans-Regular.ttf)",
        "$(rootpath //third_party/fonts/noto-sans:NotoSans-Bold.ttf)",
        "$(rootpath //core:src/main/scala/sgl/PortableFontData.scala)",
    ],
    binary = ":verify_portable_font_atlas",
    data = [
        "//core:src/main/scala/sgl/PortableFontData.scala",
        "//third_party/fonts/noto-sans:NotoSans-Bold.ttf",
        "//third_party/fonts/noto-sans:NotoSans-Regular.ttf",
    ],
)
