load("//bazel:scalajs.bzl", "scalajs_module")
load("//bazel/scalajs:defs.bzl", "run_server")

def _html5_main_template_impl(ctx):
    """Implementation for scalajs_html_template rule."""
    
    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": ctx.attr.core_abstract_class,
        "{{CANVAS_WIDTH}}": str(ctx.attr.canvas_width),
        "{{CANVAS_HEIGHT}}": str(ctx.attr.canvas_height),
        "{{ASSETS_SERVING_ROOT}}": str(ctx.attr.assets_serving_root),
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
        "assets_serving_root": attr.string(
            mandatory = False,
            doc = "The resource root used to serve static assets (the game will fetch them with this prefix)",
            default = 'static',
        ),
        "canvas_width": attr.int(
            mandatory = False,
            doc = "The width of the canvas that will be provided on the HTML page, in pixels.",
            default = 800,
        ),
        "canvas_height": attr.int(
            mandatory = False,
            doc = "The height of the canvas that will be provided on the HTML page, in pixels.",
            default = 600,
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
  assets = [],
  assets_strip_prefix = '',
  static_folder = 'static',
  output_name = "index.js",
  canvas_width = 800,
  canvas_height = 600,
  use_extension_tiled = False,
):

    full_deps = deps + [
          Label("//core:sgl-core"),
          Label("//html5:sgl-html5"),
          Label("//modules:sgl-scene2d"),
          Label("@maven//:org_scala_js_scalajs_dom_sjs1_2_13"),
    ]
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    html5_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        assets_serving_root = static_folder,
        canvas_width = canvas_width,
        canvas_height = canvas_height,
    )
    
    scalajs_module(
        name = name + "_indexjs",
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        output_name = output_name,
    )
    
    run_server(
        name = name + "_serve",
        scalajs_module = ":" + name + "_indexjs",
        main_class = main_class,
        static_files = assets,
        static_strip_prefix = assets_strip_prefix,
        static_folder = static_folder,
    )
