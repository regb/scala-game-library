load(
    "@rules_scala//scala:scala.bzl",
    _scala_library = "scala_library", "scala_binary",
)

def scala_library(deps = [],
                  plugins = [],
                  target_compatible_with = [],
                  **kwords):

    cross_deps = select({
        Label("//bazel/platforms:compiler_js"): deps+[Label("@maven//:org_scala_js_scalajs_library_2_13")],
        Label("//conditions:default"): deps,
    })
    cross_plugins = select({
        Label("//bazel/platforms:compiler_js"): plugins+[Label("@maven//:org_scala_js_scalajs_compiler_2_13_16")],
        Label("//conditions:default"): plugins,
    })
    cross_target_compatible_with = select({
        Label("//bazel/platforms:compiler_native"): [Label("@platforms//:incompatible")],
        Label("//conditions:default"): [],
    })
    _scala_library(
        deps = cross_deps,
        plugins = cross_plugins,
        target_compatible_with = cross_target_compatible_with,
        **kwords,
    )


