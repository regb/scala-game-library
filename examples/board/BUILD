load("@rules_scala//scala:scala.bzl", "scala_library", "scala_binary")

scala_library(
    name = "board-core",
    srcs = glob(["core/src/main/scala/**/*.scala"]),
    deps = [
        "//core:sgl-core",
    ],
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "board-desktop-awt",
    srcs = glob(["desktop-awt/src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.board.desktop.Main",
    deps = [
        ":board-core",
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "//desktop-awt:sgl-desktop-awt",
    ],
)
