load("//bazel:cross.bzl", "scala_library")
load("@rules_scala//scala:scala.bzl", "scala_test")

scala_library(
    name="sgl-scene2d",
    srcs=glob(["scene2d/src/main/scala/**/*.scala"]),
    deps = [
        "//engines:sgl-screen2d",
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_library(
    name="sgl-particles",
    srcs=glob(["particles/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)
