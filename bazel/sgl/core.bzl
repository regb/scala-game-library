load("//bazel:cross.bzl", "scala_library")

def sgl_core_library(name, srcs, deps=[]):
    scala_library(
        name = name,
        srcs = srcs,
        deps = deps + ["//core:sgl-core"],
    )
