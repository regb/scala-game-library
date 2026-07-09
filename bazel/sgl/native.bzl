load("//bazel:cross.bzl", sgl_scala_library = "scala_library")
load("//bazel:scalanative.bzl", "scala_native_binary")


def _path_to_vector(path):
    parts = [p for p in path.strip("/").split("/") if p]
    return "Vector(" + ", ".join(["\"%s\"" % p for p in parts]) + ")"


def _absolute_scala_type(type_name, default_package = None):
    if type_name.startswith("_root_."):
        return type_name
    if default_package and "." not in type_name:
        return "_root_." + default_package + "." + type_name
    return "_root_." + type_name


def _desktop_native_main_impl(ctx):
    resource_roots = ""
    if ctx.attr.resources_root:
        resource_roots = """
  override protected val NativeAssetsRoot: String = \"%s\"
""" % ctx.attr.resources_root

    tiled_mixins = "\n  with _root_.sgl.tiled.TiledMapRendererComponent\n  with _root_.sgl.tiled.TmxJsonParserComponent\n  with _root_.sgl.util.SimpleJsonProvider\n  with _root_.sgl.util.metrics.NoInstrumentationProvider" if ctx.attr.use_extension_tiled else ""
    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])

    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
        "{{BACKEND_APP}}": _absolute_scala_type(ctx.attr.backend_app, "sgl.native"),
        "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
        "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
        "{{TARGET_FPS}}": "Some(%d)" % ctx.attr.target_fps if ctx.attr.target_fps > 0 else "None",
        "{{RESOURCE_ROOTS}}": resource_roots,
        "{{SCREEN_FORCE_PPI}}": "override val ScreenForcePPI: Option[Float] = Some(%sf)" % ctx.attr.screen_force_ppi if ctx.attr.screen_force_ppi > 0 else "",
        "{{SCREEN2D_MIXIN}}": "with _root_.sgl.Screen2DGameApp" if ctx.attr.use_screen2d else "",
        "{{TILED_MIXINS}}": tiled_mixins,
        "{{EXTRA_MIXINS}}": extra_mixins,
    }

    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = output,
        substitutions = substitutions,
    )

    return [DefaultInfo(files = depset([output]))]


desktop_native_main = rule(
    implementation = _desktop_native_main_impl,
    attrs = {
        "_template": attr.label(
            default = ":DesktopNativeMain.scala.template",
            allow_single_file = True,
        ),
        "package": attr.string(mandatory = True),
        "main_class": attr.string(mandatory = True),
        "core_abstract_class": attr.string(mandatory = True),
        "backend_app": attr.string(default = "NativeApp"),
        "frame_width": attr.int(default = 800),
        "frame_height": attr.int(default = 800),
        "target_fps": attr.int(default = 60),
        "resources_root": attr.string(default = ""),
        "multi_dpi_resources_root": attr.string(default = ""),
        "use_extension_tiled": attr.bool(default = False),
        "use_screen2d": attr.bool(default = True),
        "screen_force_ppi": attr.int(default = 0),
        "extra_mixins": attr.string_list(default = [], doc = "Additional Scala traits to mix into the generated desktop app object."),
    },
    doc = "Generate a Main entrypoint for the Desktop Scala Native backend of SGL",
)


def sgl_desktop_native_app(
        name,
        deps,
        package,
        main_class,
        core_abstract_class,
        assets = [],
        assets_strip_prefix = "",
        resources_root = "",
        multi_dpi_resources_root = "",
        frame_width = 800,
        frame_height = 800,
        target_fps = 60,
        backend_app = "NativeApp",
        linkopts = None,
        use_extension_tiled = False,
        use_screen2d = True,
        screen_force_ppi = 0,
        extra_mixins = []):
    if assets and not resources_root:
        resources_root = assets_strip_prefix or native.package_name()
    if resources_root and not multi_dpi_resources_root:
        multi_dpi_resources_root = resources_root.rstrip("/") + "/drawable-mdpi"

    full_deps = deps + [
        Label("//core:sgl-core"),
        Label("//modules:sgl-scene2d"),
        Label("//modules:sgl-particles"),
        Label("//jvm-shared:jvm-shared"),
        Label("//desktop-native:sgl-desktop-native"),
    ]
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    desktop_native_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        frame_width = frame_width,
        frame_height = frame_height,
        target_fps = target_fps,
        backend_app = backend_app,
        resources_root = resources_root,
        multi_dpi_resources_root = multi_dpi_resources_root,
        use_extension_tiled = use_extension_tiled,
        use_screen2d = use_screen2d,
        screen_force_ppi = screen_force_ppi,
        extra_mixins = extra_mixins,
    )

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
        linkopts = linkopts if linkopts != None else ["-lSDL2", "-lSDL2_image", "-lGL"],
        main_class = package + ".desktop." + main_class,
    )
