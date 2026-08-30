load("//bazel:cross.bzl", "scala_library")

scala_library(
    name = "sgl-scene2d",
    srcs = glob(["scene2d/src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
        "//engines:sgl-screen2d",
    ],
)

scala_library(
    name = "sgl-particles",
    srcs = glob(["particles/src/main/scala/**/*.scala"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core:sgl-core",
    ],
)
