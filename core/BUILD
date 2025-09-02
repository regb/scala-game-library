load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_library",
    "scala_test",
)

scala_library(
    name="sgl-core",
    srcs=glob(["src/main/scala/**/*.scala"]),
    visibility=["//visibility:public"],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
    deps = [ "@maven//:org_scala_js_scalajs_library_2_13" ],
)

scala_library(
    name="sgl-core-testlib",
    srcs=glob(["src/test/scala/sgl/util/*"]),
    testonly = True,
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_2_13",
        "@rules_scala//testing/toolchain:scalatest_classpath",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-core-tests",
    srcs=glob(["src/test/scala/**/*.scala"]),
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_2_13",
    ],
    visibility=["//visibility:public"],
)
