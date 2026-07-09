load("//bazel:cross.bzl", "scala_library")
load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name="sgl-tiled",
    srcs=glob(["tiled/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_library(
    name="sgl-tiled-gameobject",
    srcs=glob(["tiled-gameobject/src/main/scala/**/*.scala"]),
    deps = [
        ":sgl-tiled",
        "//core:sgl-core",
        "//engines:sgl-gameobject",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-tiled-tests",
    srcs=glob(["tiled/src/test/scala/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps = [
        ":sgl-tiled",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
    visibility=["//visibility:public"],
)
