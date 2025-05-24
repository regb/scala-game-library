load("@rules_scala//scala:scala.bzl", "scala_binary")

scala_binary(
    name = "snake-desktop-awt",
    srcs = glob(["src/main/scala/**/*.scala"]),
    main_class = "com.regblanc.sgl.snake.desktop.Main",
    deps = [
        "//examples/snake/core:snake-core",
        "//core:sgl-core",
        "//jvm-shared:jvm-shared",
        "//desktop-awt:sgl-desktop-awt",
    ],
)
