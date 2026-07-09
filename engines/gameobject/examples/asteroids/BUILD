load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_desktop_lwjgl_app", "sgl_desktop_native_app", "sgl_html5_opengl_app")

sgl_core_library(
    name = "core",
    srcs = ["App.scala"],
    deps = ["//engines:sgl-gameobject"],
    include_default_modules = False,
)

sgl_desktop_lwjgl_app(
    name = "desktop-lwjgl",
    package = "sgl.examples.gameobject.asteroids",
    main_class = "Main",
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    deps = [":core"],
    frame_width = 800,
    frame_height = 600,
    window_title = "SGL GameObject Asteroids",
)

sgl_html5_opengl_app(
    name = "html5",
    package = "sgl.examples.gameobject.asteroids",
    main_class = "Main",
    export_name = "AsteroidsGame",
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    deps = [":core"],
    canvas_width = 800,
    canvas_height = 600,
)

sgl_desktop_native_app(
    name = "desktop-native",
    package = "sgl.examples.gameobject.asteroids",
    main_class = "Main",
    core_abstract_class = "sgl.examples.gameobject.asteroids.core.AbstractApp",
    deps = [":core"],
    backend_app = "NativeOpenGLApp",
    use_screen2d = False,
    linkopts = ["-lSDL2", "-lSDL2_image", "-lGLESv2"],
    frame_width = 800,
    frame_height = 600,
)
