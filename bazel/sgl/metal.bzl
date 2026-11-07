load("//bazel:cross.bzl", sgl_scala_library = "scala_library")
load("//bazel:scalanative.bzl", "scala_native_binary")

def _absolute_scala_type(type_name):
    return type_name if type_name.startswith("_root_.") else "_root_." + type_name

def _desktop_metal_main_impl(ctx):
    resource_root = ""
    if ctx.attr.resources_root:
        resource_root = "override protected val MetalAssetsRoot: String = \"%s\"" % ctx.attr.resources_root
    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])
    tiled_mixins = "\n  with _root_.sgl.tiled.TiledMapRendererComponent\n  with _root_.sgl.tiled.TmxJsonParserComponent\n  with _root_.sgl.util.SimpleJsonProvider\n  with _root_.sgl.util.metrics.NoInstrumentationProvider" if ctx.attr.use_extension_tiled else ""
    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = output,
        substitutions = {
            "{{PACKAGE}}": ctx.attr.package,
            "{{MAIN_CLASS}}": ctx.attr.main_class,
            "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
            "{{EXTRA_MIXINS}}": extra_mixins,
            "{{TILED_MIXINS}}": tiled_mixins,
            "{{SCREEN2D_MIXIN}}": "with _root_.sgl.Screen2DGameApp" if ctx.attr.use_screen2d else "",
            "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
            "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
            "{{TARGET_FPS}}": "Some(%d)" % ctx.attr.target_fps if ctx.attr.target_fps > 0 else "None",
            "{{WINDOW_TITLE}}": ctx.attr.window_title,
            "{{RESOURCE_ROOT}}": resource_root,
        },
    )
    return [DefaultInfo(files = depset([output]))]

desktop_metal_main = rule(
    implementation = _desktop_metal_main_impl,
    attrs = {
        "_template": attr.label(default = ":DesktopMetalMain.scala.template", allow_single_file = True),
        "package": attr.string(mandatory = True),
        "main_class": attr.string(mandatory = True),
        "core_abstract_class": attr.string(mandatory = True),
        "frame_width": attr.int(default = 800),
        "frame_height": attr.int(default = 800),
        "target_fps": attr.int(default = 60),
        "window_title": attr.string(default = "SGL Metal App"),
        "resources_root": attr.string(default = ""),
        "use_extension_tiled": attr.bool(default = False),
        "use_screen2d": attr.bool(default = False),
        "extra_mixins": attr.string_list(default = []),
    },
)

def sgl_desktop_metal_app(
        name,
        deps,
        package,
        main_class,
        core_abstract_class,
        assets = [],
        assets_strip_prefix = "",
        frame_width = 800,
        frame_height = 800,
        target_fps = 60,
        window_title = "SGL Metal App",
        use_extension_tiled = False,
        use_screen2d = False,
        extra_mixins = []):
    resources_root = assets_strip_prefix if assets else ""
    desktop_metal_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        frame_width = frame_width,
        frame_height = frame_height,
        target_fps = target_fps,
        window_title = window_title,
        resources_root = resources_root,
        use_extension_tiled = use_extension_tiled,
        use_screen2d = use_screen2d,
        extra_mixins = extra_mixins,
    )
    full_deps = deps + [
        Label("//backends/metal-native:sgl-metal-native"),
        Label("//core:sgl-core"),
    ]
    if use_screen2d:
        full_deps.extend([
            Label("//engines:sgl-screen2d"),
            Label("//modules:sgl-scene2d"),
            Label("//modules:sgl-particles"),
        ])
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))
    sgl_scala_library(
        name = name + "-lib",
        srcs = [":" + name + "_Main"],
        tags = ["manual"],
        deps = full_deps,
    )
    scala_native_binary(
        name = name,
        tags = ["manual"],
        deps = [":" + name + "-lib"],
        data = assets,
        native_srcs = [Label("//backends/metal-native:metal_bridge.m")],
        native_copts = ["-fobjc-arc", "-fmodules"],
        target_platform = Label("//bazel/platforms:scala_native_macos_arm64"),
        linkopts = [
            "-framework",
            "AppKit",
            "-framework",
            "AVFoundation",
            "-framework",
            "CoreGraphics",
            "-framework",
            "Foundation",
            "-framework",
            "ImageIO",
            "-framework",
            "Metal",
            "-framework",
            "QuartzCore",
            "-framework",
            "UniformTypeIdentifiers",
        ],
        main_class = package + ".desktop." + main_class,
    )
