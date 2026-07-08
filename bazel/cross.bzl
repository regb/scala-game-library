load(
    "@rules_scala//scala:scala.bzl",
    _scala_library = "scala_library", "scala_binary",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

def scala_library(deps = [],
                  plugins = [],
                  scalacopts = [],
                  target_compatible_with = [],
                  **kwords):

    cross_deps = select({
        Label("//bazel/platforms:compiler_js"): deps+[Label("@maven//:org_scala_js_scalajs_library_2_13")],
        Label("//bazel/platforms:compiler_native"): deps + [
            Label("@maven//:org_scala_native_scalalib_native0_5_3"),
            Label("@maven//:org_scala_native_scala3lib_native0_5_3"),
            Label("@maven//:org_scala_native_nativelib_native0_5_3"),
            Label("@maven//:org_scala_native_clib_native0_5_3"),
            Label("@maven//:org_scala_native_posixlib_native0_5_3"),
            Label("@maven//:org_scala_native_javalib_native0_5_3"),
            Label("@maven//:org_scala_native_auxlib_native0_5_3"),
        ],
        Label("//conditions:default"): deps,
    })
    cross_plugins = select({
        Label("//bazel/platforms:compiler_js"): plugins,
        Label("//bazel/platforms:compiler_native"): plugins + [Label("@maven//:org_scala_native_nscplugin_3_3_8")],
        Label("//conditions:default"): plugins,
    })
    cross_scalacopts = select({
        Label("//bazel/platforms:compiler_js"): ["-scalajs"],
        Label("//conditions:default"): [],
    })
    cross_target_compatible_with = select({
        Label("//conditions:default"): [],
    })
    _scala_library(
        deps = cross_deps,
        plugins = cross_plugins,
        scalacopts = SGL_SCALACOPTS + cross_scalacopts + scalacopts,
        target_compatible_with = cross_target_compatible_with,
        **kwords,
    )


