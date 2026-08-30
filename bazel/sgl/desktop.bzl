load("@rules_scala//scala:scala.bzl", "scala_binary")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

def _absolute_scala_type(type_name):
    return type_name if type_name.startswith("_root_.") else "_root_." + type_name

def _desktop_awt_main_impl(ctx):
    """Implementation for desktop_awt_main rule."""

    tiled_mixins = "\n  with _root_.sgl.tiled.TiledMapRendererComponent\n  with _root_.sgl.tiled.TmxJsonParserComponent" if ctx.attr.use_extension_tiled else ""
    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])

    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
        "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
        "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
        "{{TILED_MIXINS}}": tiled_mixins,
        "{{EXTRA_MIXINS}}": extra_mixins,
        "{{GAME_LOOP_STATISTICS_MIXIN}}": "with _root_.sgl.GameLoopStatisticsComponent" if ctx.attr.use_screen2d else "",
        "{{SCREEN2D_MIXIN}}": "with _root_.sgl.Screen2DGameApp" if ctx.attr.use_screen2d else "",
    }

    if ctx.attr.save_component:
        substitutions["{{SAVE_COMPONENT}}"] = _absolute_scala_type(ctx.attr.save_component)
        substitutions["{{SAVE_COMPONENT_INIT}}"] = ""
    elif ctx.attr.file_save:
        substitutions["{{SAVE_COMPONENT}}"] = "_root_.sgl.SaveComponent"
        substitutions["{{SAVE_COMPONENT_INIT}}"] = """
  type Save = _root_.sgl.desktop.FileSave
  override val Save: Save = new _root_.sgl.desktop.FileSave("{}")
  """.format(ctx.attr.file_save)
    else:
        substitutions["{{SAVE_COMPONENT}}"] = "_root_.sgl.MemorySaveComponent"
        substitutions["{{SAVE_COMPONENT_INIT}}"] = ""
    
    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = output,
        substitutions = substitutions
    )
    
    return [DefaultInfo(files = depset([output]))]

desktop_awt_main = rule(
    implementation = _desktop_awt_main_impl,
    attrs = {
        "_template": attr.label(
            default = ":DesktopAWTMain.scala.template",
            allow_single_file = True,
        ),
        "package": attr.string(
            mandatory = True,
            doc = "The base package for the game",
        ),
        "main_class": attr.string(
            mandatory = True,
            doc = "The name for the main class",
        ),
        "core_abstract_class": attr.string(
            mandatory = True,
            doc = "The fully qualified path to the core shared code",
        ),
        "file_save": attr.string(
            mandatory = False,
            doc = "A filename to use for saving the game progress. Will be written to by the game. If empty, the game will save in memory unless save_component is set.",
            default = "",
        ),
        "save_component": attr.string(
            mandatory = False,
            doc = "A SaveComponent implementation to mix into the generated desktop app, such as NoSaveComponent or MemorySaveComponent. If set, file_save is ignored.",
            default = "",
        ),
        "frame_width": attr.int(
            mandatory = False,
            doc = "The width of the frame that will be created on the system, in pixels.",
        ),
        "frame_height": attr.int(
            mandatory = False,
            doc = "The height of the frame that will be created on the system, in pixels.",
        ),
        "use_extension_tiled": attr.bool(
            mandatory = False,
            default = False,
        ),
        "use_screen2d": attr.bool(
            mandatory = False,
            default = True,
        ),
        "extra_mixins": attr.string_list(
            mandatory = False,
            default = [],
            doc = "Additional Scala traits to mix into the generated desktop app object.",
        ),
    },
    doc = "Generate a Main entrypoint for the Desktop AWT backend of SGL",
)

def sgl_desktop_awt_app(
  name,
  deps,
  package,
  main_class,
  core_abstract_class,
  file_save = "",
  save_component = "",
  frame_width = 800,
  frame_height = 800,
  use_extension_tiled = False,
  use_screen2d = True,
  extra_mixins = [],
):

    full_deps = deps + [
          Label("//core:sgl-core"),
          Label("//backends/jvm-shared:jvm-shared"),
          Label("//backends/desktop-jvm-shared:desktop-jvm-shared"),
          Label("//backends/desktop-awt:sgl-desktop-awt"),
    ]
    if use_screen2d:
        full_deps.extend([
            Label("//engines:sgl-screen2d"),
            Label("//modules:sgl-scene2d"),
            Label("//modules:sgl-particles"),
        ])
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    desktop_awt_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        file_save = file_save,
        save_component = save_component,
        frame_width = frame_width,
        frame_height = frame_height,
        use_extension_tiled = use_extension_tiled,
        use_screen2d = use_screen2d,
        extra_mixins = extra_mixins,
    )
    
    scala_binary(
        name = name,
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        scalacopts = SGL_SCALACOPTS,
        main_class = package + ".desktop." + main_class
    )
