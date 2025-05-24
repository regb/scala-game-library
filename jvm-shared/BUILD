load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_library",
    "scala_test",
)

scala_library(
    name="jvm-shared",
    srcs=glob(["src/main/**/*.scala"]),
    deps=[
        "//core:sgl-core",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="jvm-shared-tests",
    srcs=glob(["src/test/scala/**/*.scala"]),
    deps = [
        ":jvm-shared",
        "//core:sgl-core",
        "//core:sgl-core-tests",
        "@maven//:org_scalatest_scalatest_2_13",
    ],
    visibility=["//visibility:public"],
)
