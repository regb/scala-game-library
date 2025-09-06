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
            default = "//bazel/scalajs:default.html.template",
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

def run_server(name, scalajs_module, main_class, static_files = [], static_strip_prefix = '', static_folder = 'static', template = None, **kwargs):
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
    
    # Generate static file copy commands
    static_copy_commands = ""
    if static_files:
        for static_file in static_files:
            static_copy_commands += """
# Copy static file: {}
STATIC_FILE=$(location {})
""".format(static_file, static_file)
            
            if static_strip_prefix:
                # Look for the prefix in the path and strip everything before it (including the prefix itself)
                prefix_escaped = static_strip_prefix.rstrip('/').replace('/', '\\/')
                static_copy_commands += """if echo "$$STATIC_FILE" | grep -q "{}"; then
    STATIC_TARGET_PATH=$$(echo "$$STATIC_FILE" | sed 's|.*{}/||')
else
    echo "Error: static_strip_prefix '{}' not found in file path: $$STATIC_FILE"
    exit 1
fi
""".format(prefix_escaped, prefix_escaped, static_strip_prefix)
            else:
                # If no prefix specified, use the filename only
                static_copy_commands += """STATIC_TARGET_PATH=$$(basename "$$STATIC_FILE")
"""
            
            static_copy_commands += """STATIC_DIR="$$TEMP_DIR/{}/$$(dirname "$$STATIC_TARGET_PATH")"
mkdir -p "$$STATIC_DIR"
cp "$$STATIC_FILE" "$$TEMP_DIR/{}/$$STATIC_TARGET_PATH"
""".format(static_folder, static_folder)
    
    native.genrule(
        name = name,
        outs = [name + "_runner.sh"],
        srcs = srcs,
        cmd = """
cat > $@ << 'EOF'
#!/bin/bash

TEMP_DIR=$$(mktemp -d)

cleanup() {{
    echo "Cleaning up temporary directory: $$TEMP_DIR"
    if [[ -n $$SERVER_PID ]]; then
        kill $$SERVER_PID 2>/dev/null
        wait $$SERVER_PID 2>/dev/null
    fi
    rm -rf "$$TEMP_DIR"
}}
trap cleanup EXIT INT TERM

cp $(location :{}) "$$TEMP_DIR/index.html"
JS_FILE=$(location {})
JS_TARGET_PATH=$$(echo "$$JS_FILE" | sed 's|.*bazel-out/[^/]*/bin/||')
JS_DIR="$$TEMP_DIR/$$(dirname "$$JS_TARGET_PATH")"
mkdir -p "$$JS_DIR"
cp "$$JS_FILE" "$$TEMP_DIR/$$JS_TARGET_PATH"
{}
$(location //bazel/scalajs:server) "$$TEMP_DIR" &
SERVER_PID=$$!

wait $$SERVER_PID
EOF
chmod +x $@
        """.format(html_target_name, scalajs_module, static_copy_commands),
        tools = ["//bazel/scalajs:server"],
        executable = True,
    )
