load("@rules_scala//scala:scala.bzl", "scala_library", "scala_binary")

scala_library(
    name = "menu-core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "menu-desktop-awt",
    srcs = glob(["desktop-awt/src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.menu.desktop.Main",
    deps = [
        ":menu-core",
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "//desktop-awt:sgl-desktop-awt",
    ],
)
