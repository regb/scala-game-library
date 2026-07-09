load("//bazel:cross.bzl", "scala_library")
load("@rules_scala//scala:scala.bzl", "scala_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name="sgl-screen2d",
    srcs=glob(["screen2d/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_library(
    name="sgl-ecs",
    srcs=glob(["ecs/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_library(
    name="sgl-gameobject",
    srcs=glob(["gameobject/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-ecs-tests",
    srcs=glob(["ecs/src/test/scala/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps = [
        ":sgl-ecs",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-gameobject-tests",
    srcs=glob(["gameobject/src/test/scala/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps = [
        ":sgl-gameobject",
        "//core:sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
    visibility=["//visibility:public"],
)
