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
)



scala_library(
    name="sgl-core-testlib",
    srcs=["src/test/scala/sgl/util/LoaderAbstractSuite.scala"],
    testonly = True,
    deps = [
        ":sgl-core",
        "@maven//:org_scalatest_scalatest_2_13",
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
