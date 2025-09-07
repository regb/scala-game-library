load("//bazel:cross.bzl", "scala_library")

def sgl_core_library(
  name,
  srcs,
  resources = [],
  resource_strip_prefix = "",
  deps=[],
  use_extension_tiled=False,
):

    # TODO: could be a nice feature to map asset to resources, figure out how.
    #resources = []
    #resource_strip_prefix = ""
    #if assets_path:
    #    resources = native.glob([assets_path.strip("/") + "/**"])
    #    full_assets_path = native.package_name() + "/" + assets_path.strip("/")
    #    resource_strip_prefix = full_assets_path

    full_deps = deps + [
        Label("//core:sgl-core"),
        # For now these modules are always included, but we might make them optional eventually.
        Label("//modules:sgl-scene2d"),
        Label("//modules:sgl-particles"),
    ]
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    scala_library(
        name = name,
        srcs = srcs,
        deps = full_deps,
        resources = resources,
        resource_strip_prefix = resource_strip_prefix,
    )

