load(
    "@rules_scala//scala:scala.bzl",
    _scala_library = "scala_library", "scala_binary",
)

def scala_library(deps = [],
                  plugins = [],
                  target_compatible_with = [],
                  **kwords):

    cross_deps = select({
        "//bazel/platforms:compiler_js": deps+["@maven//:org_scala_js_scalajs_library_2_13"],
        "//conditions:default": deps,
    })
    cross_plugins = select({
        "//bazel/platforms:compiler_js": plugins+["@maven//:org_scala_js_scalajs_compiler_2_13_16"],
        "//conditions:default": plugins,
    })
    cross_target_compatible_with = select({
        "//bazel/platforms:compiler_native": ["@platforms//:incompatible"],
        "//conditions:default": [],
    })
    _scala_library(
        deps = cross_deps,
        plugins = cross_plugins,
        target_compatible_with = cross_target_compatible_with,
        **kwords,
    )


