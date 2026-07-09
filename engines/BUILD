load("//bazel:cross.bzl", "scala_library")

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
