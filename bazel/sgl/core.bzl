load("//bazel:cross.bzl", "scala_library")
load("//bazel/sgl:assets.bzl", "sgl_assets_provider")

def sgl_core_library(
  name,
  srcs,
  assets = [],
  package = None,
  deps=[],
  use_extension_tiled=False,
  include_default_modules=True,
):

    full_deps = deps + [
        Label("//core:sgl-core"),
    ]
    if include_default_modules:
        # For now these modules are included by default for legacy examples,
        # but core-only examples can opt out.
        full_deps.extend([
            Label("//modules:sgl-scene2d"),
            Label("//modules:sgl-particles"),
        ])
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    generated_assets = []
    resources = []
    if assets:
        assets_provider_name = name + "_assets_provider"
        sgl_assets_provider(
            name = assets_provider_name,
            package = package if package else native.package_name().replace("/", "."),
            asset_groups = assets,
        )
        generated_assets = [":" + assets_provider_name]
        resources = assets

    scala_library(
        name = name,
        srcs = srcs + generated_assets,
        deps = full_deps,
        exports = full_deps,
        resources = resources,
        resource_strip_prefix = (native.package_name() + "/assets").strip("/") if assets else "",
    )

