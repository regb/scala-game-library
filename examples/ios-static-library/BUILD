load("@rules_scala//scala:scala.bzl", "scala_library")
load("@rules_shell//shell:sh_test.bzl", "sh_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel:scalanative.bzl", "scala_native_static_library")
load("//bazel/sgl:defs.bzl", "sgl_ios_metal_libraries")

exports_files([
    "IOSMetalShell.swift",
    "IOSSmoke.swift",
    "Info.plist",
    "SnakeIOSExports.scala",
    "ios_snake_runtime_test.sh",
    "build_simulator_app.sh",
    "sgl_ios.h",
    "snake_swift_link_smoke_test.sh",
    "validate_snake_frame.py",
])

scala_library(
    name = "exports",
    srcs = ["IOSExports.scala"],
    plugins = ["@maven//:org_scala_native_nscplugin_3_3_8"],
    scalacopts = SGL_SCALACOPTS,
    tags = ["manual"],
    deps = [
        "@maven//:org_scala_native_auxlib_native0_5_3",
        "@maven//:org_scala_native_clib_native0_5_3",
        "@maven//:org_scala_native_javalib_native0_5_3",
        "@maven//:org_scala_native_nativelib_native0_5_3",
        "@maven//:org_scala_native_posixlib_native0_5_3",
        "@maven//:org_scala_native_scala3lib_native0_5_3",
        "@maven//:org_scala_native_scalalib_native0_5_3",
    ],
)

sgl_ios_metal_libraries(
    name = "snake_ios",
    srcs = ["SnakeIOSExports.scala"],
    deps = ["//examples/snake:core"],
)

scala_native_static_library(
    name = "ios_simulator",
    clang = "//bazel/scalanative:xcrun-clang-iphonesimulator",
    clang_pp = "//bazel/scalanative:xcrun-clangxx-iphonesimulator",
    tags = ["manual"],
    target_platform = "//bazel/platforms:scala_native_macos_arm64",
    target_triple = "arm64-apple-ios15.0-simulator",
    deps = [":exports"],
)

sh_test(
    name = "swift_link_smoke_test",
    srcs = ["swift_link_smoke_test.sh"],
    data = [
        ":IOSMetalShell.swift",
        ":IOSSmoke.swift",
        ":ios_simulator",
        ":sgl_ios.h",
    ],
    tags = [
        "local",
        "manual",
    ],
)

sh_test(
    name = "snake_swift_link_smoke_test",
    srcs = ["snake_swift_link_smoke_test.sh"],
    data = [
        ":IOSMetalShell.swift",
        ":sgl_ios.h",
        ":snake_ios_device",
        ":snake_ios_simulator",
        "//backends/metal-native:metal_bridge.m",
    ],
    tags = [
        "local",
        "manual",
    ],
)

sh_test(
    name = "ios_snake_runtime_test",
    srcs = ["ios_snake_runtime_test.sh"],
    data = [
        ":IOSMetalShell.swift",
        ":Info.plist",
        ":sgl_ios.h",
        ":snake_ios_simulator",
        ":validate_snake_frame.py",
        "//backends/metal-native:metal_bridge.m",
    ],
    tags = [
        "local",
        "manual",
    ],
)

scala_native_static_library(
    name = "ios_device",
    clang = "//bazel/scalanative:xcrun-clang-iphoneos",
    clang_pp = "//bazel/scalanative:xcrun-clangxx-iphoneos",
    tags = ["manual"],
    target_platform = "//bazel/platforms:scala_native_macos_arm64",
    target_triple = "arm64-apple-ios15.0",
    deps = [":exports"],
)
