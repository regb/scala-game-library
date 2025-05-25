load("@rules_scala//scala:scala.bzl", "scala_library", "scala_binary")

scala_library(
    name = "hello-core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
    resources = glob(["assets/**"]),
    resource_strip_prefix = "examples/hello/assets",  
)


scala_binary(
    name = "hello-desktop-awt",
    srcs = glob(["desktop-awt/src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.test.desktop.Main",
    deps = [
        ":hello-core",
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "//desktop-awt:sgl-desktop-awt",
    ],
)
