load("//bazel:cross.bzl", sgl_scala_library = "scala_library")
load("//bazel:scalanative.bzl", "scala_native_binary")


def _path_to_vector(path):
    parts = [p for p in path.strip("/").split("/") if p]
    return "Vector(" + ", ".join(["\"%s\"" % p for p in parts]) + ")"


def _desktop_native_main_impl(ctx):
    resource_roots = ""
    if ctx.attr.resources_root:
        resource_roots = """
  override val ResourcesRoot: ResourcePath = PartsResourcePath(%s)
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(%s)
""" % (
            _path_to_vector(ctx.attr.resources_root),
            _path_to_vector(ctx.attr.multi_dpi_resources_root or (ctx.attr.resources_root.rstrip("/") + "/drawable-mdpi")),
        )

    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": ctx.attr.core_abstract_class,
        "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
        "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
        "{{TARGET_FPS}}": "Some(%d)" % ctx.attr.target_fps if ctx.attr.target_fps > 0 else "None",
        "{{RESOURCE_ROOTS}}": resource_roots,
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
        "frame_width": attr.int(default = 800),
        "frame_height": attr.int(default = 800),
        "target_fps": attr.int(default = 60),
        "resources_root": attr.string(default = ""),
        "multi_dpi_resources_root": attr.string(default = ""),
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
        use_extension_tiled = False):
    if assets and not resources_root:
        resources_root = assets_strip_prefix or native.package_name()
    if resources_root and not multi_dpi_resources_root:
        multi_dpi_resources_root = resources_root.rstrip("/") + "/drawable-mdpi"

    full_deps = deps + [
        Label("//core:sgl-core"),
        Label("//modules:sgl-scene2d"),
        Label("//modules:sgl-particles"),
        Label("//jvm-shared:jvm-shared"),
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
        resources_root = resources_root,
        multi_dpi_resources_root = multi_dpi_resources_root,
    )

    sgl_scala_library(
        name = name + "-lib",
        srcs = [":" + name + "_Main", "//desktop-native:srcs"],
        tags = ["manual"],
        deps = full_deps,
    )

    scala_native_binary(
        name = name,
        tags = ["manual"],
        deps = [":" + name + "-lib"],
        data = assets,
        main_class = package + ".desktop." + main_class,
    )
