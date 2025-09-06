load("//bazel:cross.bzl", "scala_library")

def sgl_core_library(name, srcs, assets_path=None, deps=[]):

    resources = []
    resource_strip_prefix = ""
    if assets_path:
        resources = native.glob([assets_path.strip("/") + "/**"])
        full_assets_path = native.package_name() + "/" + assets_path.strip("/")
        resource_strip_prefix = full_assets_path

    scala_library(
        name = name,
        srcs = srcs,
        deps = deps + [Label("//core:sgl-core")],
        resources = resources,
        resource_strip_prefix = resource_strip_prefix,
    )

