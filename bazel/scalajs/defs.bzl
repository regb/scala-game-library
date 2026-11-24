def _static_destination(src, strip_prefix):
    path = src.short_path
    if not strip_prefix:
        return src.basename
    index = path.find(strip_prefix)
    if index < 0:
        fail("static_strip_prefix '%s' not found in file path: %s" % (strip_prefix, path))
    return path[index + len(strip_prefix):].lstrip("/")

def _escape_html(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

def _scalajs_site_impl(ctx):
    js = ctx.file.scalajs_module
    page = ctx.actions.declare_file(ctx.attr.name + "_page/" + ctx.attr.html_output_name)
    substitutions = {
        "{{JS_PATH}}": js.basename,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{PAGE_TITLE}}": _escape_html(ctx.attr.page_title),
    }
    ctx.actions.expand_template(template = ctx.file.template, output = page, substitutions = substitutions)

    entries = [(page, ctx.attr.html_output_name), (js, js.basename)]
    static_folder = ctx.attr.static_folder.strip("/")
    for src in ctx.files.static_files:
        entries.append((src, static_folder + "/" + _static_destination(src, ctx.attr.static_strip_prefix)))
    for src in ctx.files.site_files:
        entries.append((src, src.basename))

    destinations = {}
    for src, destination in entries:
        if destination in destinations:
            fail("Two files would be placed at '%s' in the site: %s and %s" % (destination, destinations[destination], src.short_path))
        destinations[destination] = src.short_path

    site = ctx.actions.declare_directory(ctx.attr.name)
    args = ctx.actions.args()
    args.add(site.path)
    for src, destination in entries:
        args.add(src)
        args.add(destination)
    ctx.actions.run_shell(
        inputs = [src for src, _ in entries],
        outputs = [site],
        arguments = [args],
        command = """
set -eu
site="$1"
shift
while [ "$#" -gt 0 ]; do
  mkdir -p "$site/$(dirname "$2")"
  cp "$1" "$site/$2"
  chmod 644 "$site/$2"
  shift 2
done
""",
        mnemonic = "ScalaJsSite",
        progress_message = "Assembling the website %{label}",
    )
    return [DefaultInfo(files = depset([site]))]

scalajs_site = rule(
    implementation = _scalajs_site_impl,
    attrs = {
        "scalajs_module": attr.label(mandatory = True, allow_single_file = [".js"], doc = "The linked Scala.js module, placed at the site root."),
        "main_class": attr.string(mandatory = True, doc = "The exported JavaScript name, substituted for {{MAIN_CLASS}} in the template."),
        "page_title": attr.string(default = "SGL Game", doc = "The page title, HTML-escaped and substituted for {{PAGE_TITLE}} in the template."),
        "template": attr.label(
            default = Label("//bazel/scalajs:default.html.template"),
            allow_single_file = True,
            doc = "The HTML page. {{JS_PATH}} is replaced by the module file name, {{MAIN_CLASS}} by main_class, and {{PAGE_TITLE}} by page_title.",
        ),
        "html_output_name": attr.string(default = "index.html", doc = "The file name of the page in the site."),
        "static_files": attr.label_list(allow_files = True, doc = "Files placed under static_folder."),
        "static_strip_prefix": attr.string(default = "", doc = "Path prefix removed from static files. Without it, static files are placed by file name."),
        "static_folder": attr.string(default = "static", doc = "The site folder holding the static files."),
        "site_files": attr.label_list(allow_files = True, doc = "Extra files (icons, images for the page) placed at the site root by file name."),
    },
    doc = "Assembles a Scala.js application as a static website directory, ready to deploy.",
)

def _scalajs_dev_server_impl(ctx):
    site = ctx.file.site
    server = ctx.executable._server
    runner = ctx.actions.declare_file(ctx.attr.name + "_runner.sh")
    ctx.actions.write(
        output = runner,
        is_executable = True,
        content = """#!/bin/bash
SITE_DIR="{site}" exec "{server}" "$@"
""".format(site = site.short_path, server = server.short_path),
    )
    runfiles = ctx.runfiles(files = [site]).merge(ctx.attr._server[DefaultInfo].default_runfiles)
    return [DefaultInfo(executable = runner, runfiles = runfiles)]

scalajs_dev_server = rule(
    implementation = _scalajs_dev_server_impl,
    executable = True,
    attrs = {
        "site": attr.label(mandatory = True, allow_single_file = True, doc = "The scalajs_site to serve."),
        "_server": attr.label(default = Label("//bazel/scalajs:server"), executable = True, cfg = "target"),
    },
    doc = "Serves a scalajs_site on http://localhost:8080 (PORT overrides the port).",
)

def run_server(name, scalajs_module, main_class, static_files = [], static_strip_prefix = "", static_folder = "static", template = None, html_output_name = "index.html", site_files = [], page_title = "SGL Game", **kwargs):
    """Builds a deployable website for a Scala.js application and a server for it.

    `<name>_site` is the website directory: the page, the JavaScript module at its
    root, the static files under `static_folder`, and `site_files` at the root.
    `<name>` serves that same directory with `bazel run`.

    Args:
        name: The name of the server target
        scalajs_module: The scalajs_module target to serve
        main_class: The main class name for the ScalaJS application
        static_files: List of files to be served statically.
        static_strip_prefix: A prefix that will be stripped from the list of static files.
        static_folder: A folder that will be created to place the static assets and serve them from.
        template: Optional custom HTML template (uses default if not provided)
        html_output_name: The file name of the page.
        site_files: Extra files placed at the root of the site by file name.
        page_title: The title of the page.
        **kwargs: Additional arguments passed to both targets (visibility, tags)
    """
    site_attrs = {}
    if template:
        site_attrs["template"] = template

    scalajs_site(
        name = name + "_site",
        scalajs_module = scalajs_module,
        main_class = main_class,
        static_files = static_files,
        static_strip_prefix = static_strip_prefix,
        static_folder = static_folder,
        html_output_name = html_output_name,
        site_files = site_files,
        page_title = page_title,
        **(site_attrs | kwargs)
    )

    scalajs_dev_server(
        name = name,
        site = ":" + name + "_site",
        **kwargs
    )
