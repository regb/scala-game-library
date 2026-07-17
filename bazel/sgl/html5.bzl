load("//bazel:scalajs.bzl", "scalajs_module")
load("//bazel/scalajs:defs.bzl", "run_server")

_ASSET_PACK_NAME = "sgl-assets.pack"
_ASSET_PACK_MANIFEST_NAME = "sgl-assets-manifest.json"

def _html5_asset_pack_impl(ctx):
    pack = ctx.actions.declare_file(ctx.attr.name + "/" + _ASSET_PACK_NAME)
    manifest = ctx.actions.declare_file(ctx.attr.name + "/" + _ASSET_PACK_MANIFEST_NAME)
    inputs_manifest = ctx.actions.declare_file(ctx.attr.name + "_inputs.txt")

    lines = []
    for src in ctx.files.srcs:
        logical_path = src.short_path
        if ctx.attr.assets_strip_prefix:
            marker = ctx.attr.assets_strip_prefix + "/"
            index = logical_path.find(marker)
            if index >= 0:
                logical_path = logical_path[index + len(marker):]
        logical_path = logical_path.lstrip("/")
        key = ctx.attr.static_folder.rstrip("/") + "/" + logical_path
        lines.append(src.path + "\t" + key)

    ctx.actions.write(inputs_manifest, "\n".join(lines))
    ctx.actions.run_shell(
        inputs = ctx.files.srcs + [inputs_manifest],
        outputs = [pack, manifest],
        command = """
python3 - <<'PY'
import json

inputs = "{inputs}"
pack_path = "{pack}"
manifest_path = "{manifest}"
pack_name = "{pack_name}"

entries = {{}}
offset = 0
with open(pack_path, "wb") as pack_file:
    with open(inputs, "r", encoding="utf-8") as inputs_file:
        for line in inputs_file:
            line = line.rstrip("\\n")
            if not line:
                continue
            src, key = line.split("\\t", 1)
            with open(src, "rb") as src_file:
                data = src_file.read()
            pack_file.write(data)
            entries[key] = {{
                "pack": pack_name,
                "offset": offset,
                "length": len(data),
            }}
            offset += len(data)

with open(manifest_path, "w", encoding="utf-8") as manifest_file:
    json.dump({{"entries": entries}}, manifest_file, separators=(",", ":"), sort_keys=True)
PY
        """.format(
            inputs = inputs_manifest.path,
            pack = pack.path,
            manifest = manifest.path,
            pack_name = _ASSET_PACK_NAME,
        ),
    )

    return [DefaultInfo(files = depset([pack, manifest]))]

html5_asset_pack = rule(
    implementation = _html5_asset_pack_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = True),
        "assets_strip_prefix": attr.string(default = ""),
        "static_folder": attr.string(default = "static"),
    },
    doc = "Packages HTML5 static assets into one binary pack plus a manifest.",
)

def _html5_main_template_impl(ctx):
    """Implementation for scalajs_html_template rule."""
    
    if ctx.attr.use_provided_canvas:
        theme_expression = "new ProvidedCanvasTheme"
    elif ctx.attr.fullscreen:
        theme_expression = "new FullScreenTheme"
    else:
        theme_expression = """new FixedWindowTheme {
    override val frameSize: (Int, Int) = (%s, %s)
  }""" % (ctx.attr.canvas_width, ctx.attr.canvas_height)

    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": ctx.attr.core_abstract_class,
        "{{THEME_EXPRESSION}}": theme_expression,
        "{{ASSETS_SERVING_ROOT}}": str(ctx.attr.assets_serving_root),
        "{{ASSET_PACK_MANIFEST_OVERRIDE}}": ctx.attr.asset_pack_manifest_override,
        "{{SAVE_COMPONENT}}": ctx.attr.save_component,
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
        "asset_pack_manifest_override": attr.string(
            mandatory = False,
            doc = "Generated Scala override configuring an HTML5 asset pack manifest.",
            default = "",
        ),
        "fullscreen": attr.bool(
            mandatory = False,
            doc = "Use the HTML5 full-screen theme instead of a fixed-size canvas.",
            default = False,
        ),
        "use_provided_canvas": attr.bool(
            mandatory = False,
            doc = "Use the canvas element as laid out by the hosting page.",
            default = False,
        ),
        "save_component": attr.string(
            mandatory = False,
            doc = "Save component mixed into the generated HTML5 main object.",
            default = "LocalStorageSaveComponent",
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
  pack_assets = False,
  save_component = "LocalStorageSaveComponent",
  fullscreen = False,
  use_provided_canvas = False,
):

    full_deps = deps + [
          Label("//core:sgl-core"),
          Label("//html5:sgl-html5"),
          Label("//modules:sgl-scene2d"),
          Label("@maven//:org_scala_js_scalajs_dom_sjs1_3"),
    ]
    if use_extension_tiled:
        full_deps.append(Label("//extensions:sgl-tiled"))

    asset_pack_manifest_override = ""
    static_files = assets
    static_strip_prefix = assets_strip_prefix
    if pack_assets:
        html5_asset_pack(
            name = name + "_asset_pack",
            srcs = assets,
            assets_strip_prefix = assets_strip_prefix,
            static_folder = static_folder,
        )
        static_files = [":" + name + "_asset_pack"]
        static_strip_prefix = ""
        asset_pack_manifest_override = """
  override protected val Html5AssetPackManifestPath: Option[ResourcePath] =
    Some(PartsResourcePath(Vector(\"%s\", \"%s\")))
  override protected val Html5AssetPacksFallbackToServer: Boolean = false""" % (static_folder, _ASSET_PACK_MANIFEST_NAME)

    html5_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        assets_serving_root = static_folder,
        canvas_width = canvas_width,
        canvas_height = canvas_height,
        asset_pack_manifest_override = asset_pack_manifest_override,
        save_component = save_component,
        fullscreen = fullscreen,
        use_provided_canvas = use_provided_canvas,
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
        static_files = static_files,
        static_strip_prefix = static_strip_prefix,
        static_folder = static_folder,
    )
