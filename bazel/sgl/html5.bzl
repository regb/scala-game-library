load("//bazel:scalajs.bzl", "scalajs_module")
load("//bazel/scalajs:defs.bzl", "run_server")

def _html5_main_template_impl(ctx):
    """Implementation for scalajs_html_template rule."""
    
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

html5_main = rule(
    implementation = _html5_main_template_impl,
    attrs = {
        "_template": attr.label(
            default = ":Html5Main.scala.template",
            allow_single_file = True,
        ),
        "package": attr.string(
            mandatory = True,
            doc = "The base package for the game",
        ),
        "main_class": attr.string(
            mandatory = True,
            doc = "The name for the main class (will be exported as a global javascript object)",
        ),
        "core_abstract_class": attr.string(
            mandatory = True,
            doc = "The fully qualified path to the core shared code",
        ),
    },
    doc = "Generate a Main entrypoint for the HTML5 backend of SGL",
)

def sgl_html5_app(
  name,
  deps,
  package,
  main_class,
  core_abstract_class,
  output_name = "index.js",
):
    html5_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
    )
    
    scalajs_module(
        name = name + "_indexjs",
        srcs = [":" + name + "_Main"],
        deps = deps + [
          Label("//core:sgl-core"),
          Label("//html5:sgl-html5"),
          Label("@maven//:org_scala_js_scalajs_dom_sjs1_2_13"),
        ],
        output_name = output_name,
    )
    
    run_server(
        name = name + "_serve",
        scalajs_module = ":" + name + "_indexjs",
        main_class = main_class,
    )
