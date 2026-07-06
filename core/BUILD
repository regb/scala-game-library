load("//bazel:cross.bzl", "scala_library")
load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_test",
)
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

scala_library(
    name="sgl-core",
    srcs=glob(["src/main/scala/**/*.scala"]),
    visibility=["//visibility:public"],
)

scala_library(
    name="sgl-core-testlib",
    srcs=glob(["src/test/scala/sgl/util/*"]),
    testonly = True,
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_3",
        "@rules_scala//testing/toolchain:scalatest_classpath",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-core-tests",
    srcs=glob(["src/test/scala/**/*.scala"]),
    scalacopts=SGL_SCALACOPTS,
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_3",
    ],
    visibility=["//visibility:public"],
)
