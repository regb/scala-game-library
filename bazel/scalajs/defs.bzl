def _scalajs_html_template_impl(ctx):
    """Implementation for scalajs_html_template rule."""

    js_file = ctx.attr.scalajs_target.files.to_list()[0]

    output = ctx.actions.declare_file(ctx.attr.output_name)

    substitutions = {
        "{{JS_PATH}}": js_file.short_path,
    }
    if ctx.attr.main_class:
        substitutions["{{MAIN_CLASS}}"] = ctx.attr.main_class

    substitutions.update(ctx.attr.substitutions)

    ctx.actions.expand_template(
        template = ctx.file.template,
        output = output,
        substitutions = substitutions,
    )

    return [DefaultInfo(files = depset([output]))]

scalajs_html_template = rule(
    implementation = _scalajs_html_template_impl,
    attrs = {
        "template": attr.label(
            default = Label("//bazel/scalajs:default.html.template"),
            allow_single_file = True,
            doc = "The HTML template file with placeholders (defaults to built-in template)",
        ),
        "scalajs_target": attr.label(
            mandatory = True,
            doc = "The scalajs_link target that produces the JavaScript file",
        ),
        "main_class": attr.string(
            doc = "The main class name to substitute in the template",
        ),
        "substitutions": attr.string_dict(
            default = {},
            doc = "Additional substitutions to make in the template",
        ),
        "output_name": attr.string(
            mandatory = True,
            doc = "The name of the output HTML file",
        ),
    },
    doc = "Generates an HTML file by substituting the ScalaJS output path and other values into a template",
)

def run_server(name, scalajs_module, main_class, static_files = [], static_strip_prefix = "", static_folder = "static", template = None, **kwargs):
    """Creates a server target that serves a ScalaJS application with generated HTML.

    Args:
        name: The name of the server target
        scalajs_module: The scalajs_module target to serve
        main_class: The main class name for the ScalaJS application
        static_files: List of files to be served statically.
        static_strip_prefix: A prefix that will be stripped from the list of static files.
        static_folder: A folder that will be created to place the static assets and serve them from.
        template: Optional custom HTML template (uses default if not provided)
        **kwargs: Additional arguments passed to the server rule
    """

    html_target_name = name + "_html"

    html_attrs = {
        "scalajs_target": scalajs_module,
        "main_class": main_class,
        "output_name": "index.html",
    }

    if template:
        html_attrs["template"] = template

    scalajs_html_template(
        name = html_target_name,
        **html_attrs
    )

    # Prepare srcs list with JavaScript module, HTML, and static files
    srcs = [
        scalajs_module,
        ":" + html_target_name,
    ]
    if static_files:
        srcs.extend(static_files)

    # Build static files list for environment variable
    static_files_env = ""
    if static_files:
        static_files_locations = ["$(locations {})".format(f) for f in static_files]
        static_files_env = ";".join(static_files_locations)

    # Build environment variable assignments
    env_vars = [
        "HTML_FILE=$(location :{})".format(html_target_name),
        "JS_FILE=$(location {})".format(scalajs_module),
    ]

    if static_files_env:
        env_vars.append("STATIC_FILES='{}'".format(static_files_env))

    if static_strip_prefix:
        env_vars.append("STATIC_STRIP_PREFIX='{}'".format(static_strip_prefix))

    if static_folder != "static":
        env_vars.append("STATIC_FOLDER='{}'".format(static_folder))

    env_assignments = " ".join(env_vars)

    server_target = Label("//bazel/scalajs:server")

    native.genrule(
        name = name,
        outs = [name + "_runner.sh"],
        srcs = srcs,
        cmd = """
cat > $@ << 'EOF'
#!/bin/bash
{} $(location {})
EOF
chmod +x $@
        """.format(env_assignments, server_target),
        tools = [server_target],
        executable = True,
    )
