load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    include_default_modules = False,
    deps = ["//engines:sgl-gameobject"],
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    frame_height = 600,
    frame_width = 800,
    main_class = "Main",
    package = "sgl.examples.gameobject.asteroids",
    window_title = "SGL GameObject Asteroids",
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "html5",
    canvas_height = 600,
    canvas_width = 800,
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    export_name = "AsteroidsGame",
    main_class = "Main",
    package = "sgl.examples.gameobject.asteroids",
    deps = [":core"],
)

sgl_desktop_native_app(
    name = "desktop-native",
    backend_app = "NativeOpenGLApp",
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    frame_height = 600,
    frame_width = 800,
    linkopts = [
        "-lSDL2",
        "-lSDL2_image",
        "-lGLESv2",
    ],
    main_class = "Main",
    package = "sgl.examples.gameobject.asteroids",
    use_screen2d = False,
    deps = [":core"],
)
