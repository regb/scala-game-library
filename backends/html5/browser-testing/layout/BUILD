load("//backends/html5/browser-testing:defs.bzl", "html5_browser_test")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")
load("//bazel/sgl:defs.bzl", "sgl_core_library", "sgl_html5_app", "sgl_html5_opengl_app")

sgl_core_library(
    name = "core",
    srcs = ["InputProbe.scala"],
    package = "sgl.html5.testing.layout.core",
    deps = [],
)

LAYOUTS = [
    "centered",
    "fixed",
    "fullscreen",
    "provided",
]

[sgl_html5_app(
    name = "canvas-" + layout,
    canvas_height = 200,
    canvas_width = 300,
    center_canvas = layout != "fixed",
    core_abstract_class = "sgl.html5.testing.layout.core.InputProbe",
    fullscreen = layout == "fullscreen",
    html_output_name = "index.html",
    html_template = "page.html",
    main_class = "Probe",
    output_name = "canvas-" + layout + ".js",
    package = "sgl.html5.testing.layout.canvas",
    use_provided_canvas = layout == "provided",
    use_screen2d = False,
    deps = [":core"],
) for layout in LAYOUTS]

[sgl_html5_opengl_app(
    name = "webgl-" + layout,
    canvas_height = 200,
    canvas_width = 300,
    center_canvas = layout != "fixed",
    core_abstract_class = "sgl.html5.testing.layout.core.InputProbe",
    export_name = "Probe",
    fullscreen = layout == "fullscreen",
    html_output_name = "index.html",
    html_template = "page.html",
    main_class = "Main",
    output_name = "webgl-" + layout + ".js",
    package = "sgl.html5.testing.layout.webgl",
    use_provided_canvas = layout == "provided",
    deps = [":core"],
) for layout in LAYOUTS]

sgl_html5_app(
    name = "canvas-focused",
    canvas_height = 200,
    canvas_width = 300,
    center_canvas = False,
    core_abstract_class = "sgl.html5.testing.layout.core.InputProbe",
    html_output_name = "index.html",
    html_template = "page.html",
    keyboard_capture = "canvas_focused",
    main_class = "Probe",
    output_name = "canvas-focused.js",
    package = "sgl.html5.testing.layout.canvas",
    use_screen2d = False,
    deps = [":core"],
)

sgl_html5_opengl_app(
    name = "webgl-focused",
    canvas_height = 200,
    canvas_width = 300,
    center_canvas = False,
    core_abstract_class = "sgl.html5.testing.layout.core.InputProbe",
    export_name = "Probe",
    html_output_name = "index.html",
    html_template = "page.html",
    keyboard_capture = "canvas_focused",
    main_class = "Main",
    output_name = "webgl-focused.js",
    package = "sgl.html5.testing.layout.webgl",
    deps = [":core"],
)

html5_browser_test(
    name = "keyboard-test",
    size = "medium",
    srcs = ["KeyboardTest.scala"],
    scalacopts = SGL_SCALACOPTS,
    sites = [
        ":canvas-fixed_site",
        ":canvas-focused_site",
        ":webgl-fixed_site",
        ":webgl-focused_site",
    ],
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
)

html5_browser_test(
    name = "layout-test",
    size = "medium",
    srcs = ["LayoutTest.scala"],
    scalacopts = SGL_SCALACOPTS,
    sites = [
        ":%s-%s_site" % (backend, layout)
        for backend in [
            "canvas",
            "webgl",
        ]
        for layout in LAYOUTS
    ],
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
)
