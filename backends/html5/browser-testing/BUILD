load("@rules_scala//scala:scala.bzl", "scala_library")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

# Browser automation for tests of HTML5 games, see README.md.
scala_library(
    name = "browser-testing",
    srcs = glob(["src/main/**/*.scala"]),
    data = select({
        ":linux_x86_64": ["//backends/html5/browser-testing/chrome:linux64"],
        "//conditions:default": [],
    }),
    resources = [":browser-location"],
    # It only runs where Bazel does, on a newer JVM than games support.
    scalacopts = [opt for opt in SGL_SCALACOPTS if "release" not in opt and "java-output" not in opt],
    visibility = ["//visibility:public"],
    exports = ["@rules_scala//testing/toolchain:scalatest_classpath"],
    deps = [
        "//core:sgl-core",
        "@rules_scala//testing/toolchain:scalatest_classpath",
    ],
)

config_setting(
    name = "linux_x86_64",
    constraint_values = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
)

# Where the bundled browser is among the runfiles, for the library to find.
genrule(
    name = "browser-location",
    srcs = select({
        ":linux_x86_64": ["//backends/html5/browser-testing/chrome:linux64"],
        "//conditions:default": [],
    }),
    outs = ["sgl-browser-location.txt"],
    cmd = select({
        ":linux_x86_64": "echo $(rlocationpath //backends/html5/browser-testing/chrome:linux64) > $@",
        "//conditions:default": "touch $@",
    }),
)
