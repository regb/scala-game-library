load("@rules_scala//scala:scala.bzl", "scala_binary")
load("//bazel:scala_opts.bzl", "SGL_SCALACOPTS")

def _absolute_scala_type(type_name):
    return type_name if type_name.startswith("_root_.") else "_root_." + type_name

def _desktop_lwjgl_main_impl(ctx):
    save_component = "_root_.sgl.MemorySaveComponent"
    save_component_init = ""
    if ctx.attr.file_save:
        save_component = "_root_.sgl.SaveComponent"
        save_component_init = """
  type Save = _root_.sgl.desktop.FileSave
  override val Save: Save = new _root_.sgl.desktop.FileSave(\"{}\")
""".format(ctx.attr.file_save)

    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])

    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
        "{{EXTRA_MIXINS}}": extra_mixins,
        "{{FRAME_WIDTH}}": str(ctx.attr.frame_width),
        "{{FRAME_HEIGHT}}": str(ctx.attr.frame_height),
        "{{WINDOW_TITLE}}": ctx.attr.window_title,
        "{{TARGET_FPS}}": "Some({})".format(ctx.attr.target_fps) if ctx.attr.target_fps > 0 else "None",
        "{{SAVE_COMPONENT}}": save_component,
        "{{SAVE_COMPONENT_INIT}}": save_component_init,
    }

    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = output,
        substitutions = substitutions,
    )

    return [DefaultInfo(files = depset([output]))]

desktop_lwjgl_main = rule(
    implementation = _desktop_lwjgl_main_impl,
    attrs = {
        "_template": attr.label(
            default = ":DesktopLwjglMain.scala.template",
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
        "frame_width": attr.int(
            mandatory = False,
            default = 800,
            doc = "The width of the frame that will be created on the system, in pixels.",
        ),
        "frame_height": attr.int(
            mandatory = False,
            default = 600,
            doc = "The height of the frame that will be created on the system, in pixels.",
        ),
        "window_title": attr.string(
            mandatory = False,
            default = "SGL LWJGL App",
            doc = "The title for the GLFW window.",
        ),
        "target_fps": attr.int(
            mandatory = False,
            default = 60,
            doc = "Target FPS. Use 0 for uncapped/requested by backend.",
        ),
        "file_save": attr.string(
            mandatory = False,
            default = "",
            doc = "A filename to use for saving game progress. If empty, the game will save in memory.",
        ),
        "extra_mixins": attr.string_list(
            mandatory = False,
            default = [],
            doc = "Additional Scala traits to mix into the generated desktop app object.",
        ),
    },
    doc = "Generate a Main entrypoint for the Desktop LWJGL backend of SGL",
)

def sgl_desktop_lwjgl_app(
  name,
  deps,
  package,
  main_class,
  core_abstract_class,
  data = [],
  frame_width = 800,
  frame_height = 600,
  window_title = "SGL LWJGL App",
  target_fps = 60,
  file_save = "",
  extra_mixins = [],
):
    full_deps = deps + [
        Label("//core:sgl-core"),
        Label("//backends/desktop-jvm-shared:desktop-jvm-shared"),
        Label("//backends/desktop-lwjgl:sgl-desktop-lwjgl"),
    ]

    desktop_lwjgl_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        frame_width = frame_width,
        frame_height = frame_height,
        window_title = window_title,
        target_fps = target_fps,
        file_save = file_save,
        extra_mixins = extra_mixins,
    )

    scala_binary(
        name = name,
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        data = data,
        scalacopts = SGL_SCALACOPTS,
        main_class = package + ".desktop." + main_class,
    )
