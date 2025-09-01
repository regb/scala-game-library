load(
    "@rules_scala//scala:scala.bzl",
    "scala_binary",
    "scala_library",
    "scala_test",
)

scala_library(
    name="sgl-desktop-awt",
    srcs=glob(["src/main/**/*.scala"]),
    deps=[
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "@maven//:net_liftweb_lift_json_2_13",
        "@maven//:com_googlecode_soundlibs_vorbisspi",
    ],
    visibility=["//visibility:public"],
)

scala_test(
    name="sgl-destkop-awt-tests",
    srcs=glob(["src/test/scala/**/*.scala"]),
    deps = [
        ":sgl-desktop-awt",
        "//core:sgl-core",
        "//core:sgl-core-testlib",
        "//jvm-shared:jvm-shared",
        "@maven//:org_scalatest_scalatest_2_13",
    ],
    visibility=["//visibility:public"],
)
