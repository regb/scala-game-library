load("@rules_scala//scala:scala.bzl", "scala_binary")

def _desktop_awt_main_impl(ctx):
    """Implementation for desktop_awt_main rule."""
    
    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": ctx.attr.core_abstract_class,
    }
    
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
    },
    doc = "Generate a Main entrypoint for the Desktop AWT backend of SGL",
)

def sgl_desktop_awt_app(
  name,
  deps,
  package,
  main_class,
  core_abstract_class,
):
    desktop_awt_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
    )
    
    scala_binary(
        name = name,
        srcs = [":" + name + "_Main"],
        deps = deps + [
          Label("//core:sgl-core"),
          Label("//jvm-shared:jvm-shared"),
          Label("//desktop-awt:sgl-desktop-awt"),
        ],
        main_class = package + ".desktop." + main_class
    )
