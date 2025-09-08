load("@rules_scala//scala:scala.bzl", "scala_binary")

def _desktop_awt_main_impl(ctx):
    """Implementation for desktop_awt_main rule."""
    
    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": ctx.attr.core_abstract_class,
        "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
        "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
    }

    if ctx.attr.file_save:
        substitutions["{{SAVE_COMPONENT}}"] = "SaveComponent"
        substitutions["{{SAVE_COMPONENT_INIT}}"] = """
  type Save = FileSave
  override val Save: Save = new FileSave("{}")
  """.format(ctx.attr.file_save)
    else:
        substitutions["{{SAVE_COMPONENT}}"] = "MemorySaveComponent"
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
            doc = "A filename to use for saving the game progress. Will be written to by the game. If empty, the game will save in memory.",
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
  frame_width = 800,
  frame_height = 800,
  use_extension_tiled = False,
):

    full_deps = deps + [
          Label("//core:sgl-core"),
          Label("//jvm-shared:jvm-shared"),
          Label("//desktop-awt:sgl-desktop-awt"),
          Label("//modules:sgl-scene2d"),
    ]
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    desktop_awt_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        file_save = file_save,
        frame_width = frame_width,
        frame_height = frame_height,
    )
    
    scala_binary(
        name = name,
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        main_class = package + ".desktop." + main_class
    )
