load("@rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "snake-core",
    srcs = glob(["src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
)
