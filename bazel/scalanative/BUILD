load("@rules_scala//scala:scala.bzl", "scala_binary")
load("@rules_shell//shell:sh_binary.bzl", "sh_binary")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_binary(
    name = "library_linker",
    srcs = ["ScalaNativeLibraryLinker.scala"],
    main_class = "ScalaNativeLibraryLinker",
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_scala_native_tools_3",
        "@maven//:org_scala_native_util_3",
    ],
)

[
    sh_binary(
        name = name.removesuffix(".sh"),
        srcs = [name],
        visibility = ["//visibility:public"],
    )
    for name in [
        "xcrun-clang-iphoneos.sh",
        "xcrun-clangxx-iphoneos.sh",
        "xcrun-clang-iphonesimulator.sh",
        "xcrun-clangxx-iphonesimulator.sh",
    ]
]

scala_binary(
    name = "linker",
    srcs = ["ScalaNativeLinker.scala"],
    main_class = "ScalaNativeLinker",
    scalacopts = SGL_SCALACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_scala_native_tools_3",
        "@maven//:org_scala_native_util_3",
    ],
)
