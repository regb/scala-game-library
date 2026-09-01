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

def _absolute_scala_type(type_name):
    return type_name if type_name.startswith("_root_.") else "_root_." + type_name

def _html5_scala_type(type_name):
    if type_name.startswith("_root_."):
        return type_name
    if "." in type_name:
        return "_root_." + type_name
    return "_root_.sgl.html5." + type_name

def _html5_theme_expression(ctx):
    if ctx.attr.use_provided_canvas:
        return "new _root_.sgl.html5.themes.ProvidedCanvasTheme"
    if ctx.attr.fullscreen:
        return "new _root_.sgl.html5.themes.FullScreenTheme"
    return """new _root_.sgl.html5.themes.FixedWindowTheme {
    override val frameSize: (Int, Int) = (%s, %s)
  }""" % (ctx.attr.canvas_width, ctx.attr.canvas_height)

def _html5_keyboard_capture_expression(mode):
    if mode == "global":
        return "_root_.sgl.html5.Html5KeyboardCaptureMode.Global"
    if mode == "canvas_focused":
        return "_root_.sgl.html5.Html5KeyboardCaptureMode.CanvasFocused"
    fail("keyboard_capture must be one of: global, canvas_focused")

def _scala_bool(value):
    return "true" if value else "false"

def _html5_main_template_impl(ctx):
    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])
    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
        "{{EXTRA_MIXINS}}": extra_mixins,
        "{{THEME_EXPRESSION}}": _html5_theme_expression(ctx),
        "{{KEYBOARD_CAPTURE}}": _html5_keyboard_capture_expression(ctx.attr.keyboard_capture),
        "{{KEYBOARD_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.keyboard_prevent_default),
        "{{MOUSE_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.mouse_prevent_default),
        "{{WHEEL_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.wheel_prevent_default),
        "{{TOUCH_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.touch_prevent_default),
        "{{TOUCH_STOP_PROPAGATION}}": _scala_bool(ctx.attr.touch_stop_propagation),
        "{{ASSETS_SERVING_ROOT}}": str(ctx.attr.assets_serving_root),
        "{{ASSET_PACK_MANIFEST_OVERRIDE}}": ctx.attr.asset_pack_manifest_override,
        "{{SAVE_COMPONENT}}": _html5_scala_type(ctx.attr.save_component),
        "{{GAME_LOOP_STATISTICS_MIXIN}}": "with _root_.sgl.GameLoopStatisticsComponent" if ctx.attr.use_screen2d else "",
        "{{SCREEN2D_MIXIN}}": "with _root_.sgl.Screen2DGameApp" if ctx.attr.use_screen2d else "",
    }

    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = output,
        substitutions = substitutions,
    )

    return [DefaultInfo(files = depset([output]))]

html5_main = rule(
    implementation = _html5_main_template_impl,
    attrs = {
        "_template": attr.label(default = ":Html5Main.scala.template", allow_single_file = True),
        "package": attr.string(mandatory = True, doc = "The base package for the game"),
        "main_class": attr.string(mandatory = True, doc = "The name for the main class (will be exported as a global javascript object)"),
        "core_abstract_class": attr.string(mandatory = True, doc = "The fully qualified path to the core shared code"),
        "assets_serving_root": attr.string(mandatory = False, doc = "The resource root used to serve static assets (the game will fetch them with this prefix)", default = "static"),
        "canvas_width": attr.int(mandatory = False, doc = "The width of the canvas that will be provided on the HTML page, in pixels.", default = 800),
        "canvas_height": attr.int(mandatory = False, doc = "The height of the canvas that will be provided on the HTML page, in pixels.", default = 600),
        "asset_pack_manifest_override": attr.string(mandatory = False, doc = "Generated Scala override configuring an HTML5 asset pack manifest.", default = ""),
        "use_screen2d": attr.bool(mandatory = False, default = True),
        "extra_mixins": attr.string_list(mandatory = False, default = [], doc = "Additional Scala traits to mix into the generated HTML5 app object."),
        "fullscreen": attr.bool(mandatory = False, doc = "Use the HTML5 full-screen theme instead of a fixed-size canvas.", default = False),
        "use_provided_canvas": attr.bool(mandatory = False, doc = "Use the canvas element as laid out by the hosting page.", default = False),
        "save_component": attr.string(mandatory = False, doc = "Save component mixed into the generated HTML5 main object.", default = "LocalStorageSaveComponent"),
        "keyboard_capture": attr.string(mandatory = False, doc = "Keyboard capture mode: global or canvas_focused.", default = "global", values = ["global", "canvas_focused"]),
        "keyboard_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on keyboard events captured by the game.", default = True),
        "mouse_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on mouse events captured by the game.", default = True),
        "wheel_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on wheel events captured by the game.", default = True),
        "touch_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on touch events captured by the game.", default = True),
        "touch_stop_propagation": attr.bool(mandatory = False, doc = "Call stopPropagation on touch events captured by the game.", default = True),
    },
    doc = "Generate a Main entrypoint for the HTML5 backend of SGL",
)

def _html5_opengl_main_template_impl(ctx):
    extra_mixins = "" if not ctx.attr.extra_mixins else "\n  with " + "\n  with ".join([_absolute_scala_type(m) for m in ctx.attr.extra_mixins])
    substitutions = {
        "{{PACKAGE}}": ctx.attr.package,
        "{{MAIN_CLASS}}": ctx.attr.main_class,
        "{{EXPORT_NAME}}": ctx.attr.export_name,
        "{{CORE_ABSTRACT_CLASS}}": _absolute_scala_type(ctx.attr.core_abstract_class),
        "{{EXTRA_MIXINS}}": extra_mixins,
        "{{CANVAS_WIDTH}}": str(ctx.attr.canvas_width),
        "{{CANVAS_HEIGHT}}": str(ctx.attr.canvas_height),
        "{{ASSETS_SERVING_ROOT}}": str(ctx.attr.assets_serving_root),
        "{{TARGET_FPS}}": "Some({})".format(ctx.attr.target_fps) if ctx.attr.target_fps > 0 else "None",
        "{{KEYBOARD_CAPTURE}}": _html5_keyboard_capture_expression(ctx.attr.keyboard_capture),
        "{{KEYBOARD_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.keyboard_prevent_default),
        "{{MOUSE_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.mouse_prevent_default),
        "{{WHEEL_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.wheel_prevent_default),
        "{{TOUCH_PREVENT_DEFAULT}}": _scala_bool(ctx.attr.touch_prevent_default),
        "{{TOUCH_STOP_PROPAGATION}}": _scala_bool(ctx.attr.touch_stop_propagation),
    }

    output = ctx.actions.declare_file(ctx.attr.name + ".scala")
    ctx.actions.expand_template(template = ctx.file._template, output = output, substitutions = substitutions)
    return [DefaultInfo(files = depset([output]))]

html5_opengl_main = rule(
    implementation = _html5_opengl_main_template_impl,
    attrs = {
        "_template": attr.label(default = ":Html5OpenGLMain.scala.template", allow_single_file = True),
        "package": attr.string(mandatory = True, doc = "The base package for the game"),
        "main_class": attr.string(mandatory = True, doc = "The name for the generated main object"),
        "export_name": attr.string(mandatory = True, doc = "The JavaScript export name"),
        "core_abstract_class": attr.string(mandatory = True, doc = "The fully qualified path to the core shared code"),
        "assets_serving_root": attr.string(mandatory = False, default = "static"),
        "canvas_width": attr.int(mandatory = False, default = 800),
        "canvas_height": attr.int(mandatory = False, default = 600),
        "target_fps": attr.int(mandatory = False, default = 0, doc = "Target FPS. Use 0 for requestAnimationFrame."),
        "extra_mixins": attr.string_list(mandatory = False, default = [], doc = "Additional Scala traits to mix into the generated HTML5 app object."),
        "keyboard_capture": attr.string(mandatory = False, doc = "Keyboard capture mode: global or canvas_focused.", default = "global", values = ["global", "canvas_focused"]),
        "keyboard_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on keyboard events captured by the game.", default = True),
        "mouse_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on mouse events captured by the game.", default = True),
        "wheel_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on wheel events captured by the game.", default = True),
        "touch_prevent_default": attr.bool(mandatory = False, doc = "Call preventDefault on touch events captured by the game.", default = True),
        "touch_stop_propagation": attr.bool(mandatory = False, doc = "Call stopPropagation on touch events captured by the game.", default = True),
    },
    doc = "Generate a Main entrypoint for the HTML5 OpenGL/WebGL backend of SGL",
)

def sgl_html5_app(
        name,
        deps,
        package,
        main_class,
        core_abstract_class,
        assets = [],
        assets_strip_prefix = "",
        static_folder = "static",
        output_name = "index.js",
        html_output_name = "index.html",
        canvas_width = 800,
        canvas_height = 600,
        use_extension_tiled = False,
        pack_assets = False,
        use_screen2d = True,
        extra_mixins = [],
        save_component = "LocalStorageSaveComponent",
        fullscreen = False,
        use_provided_canvas = False,
        keyboard_capture = "global",
        keyboard_prevent_default = True,
        mouse_prevent_default = True,
        wheel_prevent_default = True,
        touch_prevent_default = True,
        touch_stop_propagation = True):
    full_deps = deps + [
        Label("//core:sgl-core"),
        Label("//backends/html5:sgl-html5"),
        Label("@maven//:org_scala_js_scalajs_dom_sjs1_3"),
    ]
    if use_screen2d:
        full_deps.extend([
            Label("//engines:sgl-screen2d"),
            Label("//modules:sgl-scene2d"),
            Label("//modules:sgl-particles"),
        ])
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
  override protected val Html5AssetPackManifestResourceName: Option[String] = Some(\"%s\")
  override protected val Html5AssetPacksFallbackToServer: Boolean = false""" % (_ASSET_PACK_MANIFEST_NAME)

    html5_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        core_abstract_class = core_abstract_class,
        assets_serving_root = static_folder,
        canvas_width = canvas_width,
        canvas_height = canvas_height,
        asset_pack_manifest_override = asset_pack_manifest_override,
        use_screen2d = use_screen2d,
        extra_mixins = extra_mixins,
        save_component = save_component,
        fullscreen = fullscreen,
        use_provided_canvas = use_provided_canvas,
        keyboard_capture = keyboard_capture,
        keyboard_prevent_default = keyboard_prevent_default,
        mouse_prevent_default = mouse_prevent_default,
        wheel_prevent_default = wheel_prevent_default,
        touch_prevent_default = touch_prevent_default,
        touch_stop_propagation = touch_stop_propagation,
    )

    scalajs_module(
        name = name + "_indexjs",
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        output_name = output_name,
    )

    run_server(
        name = name,
        scalajs_module = ":" + name + "_indexjs",
        main_class = main_class,
        static_files = static_files,
        static_strip_prefix = static_strip_prefix,
        static_folder = static_folder,
        html_output_name = html_output_name,
    )

def sgl_html5_opengl_app(
        name,
        deps,
        package,
        main_class,
        export_name,
        core_abstract_class,
        assets = [],
        assets_strip_prefix = "",
        static_folder = "static",
        output_name = "index.js",
        html_output_name = "index.html",
        canvas_width = 800,
        canvas_height = 600,
        target_fps = 0,
        extra_mixins = [],
        keyboard_capture = "global",
        keyboard_prevent_default = True,
        mouse_prevent_default = True,
        wheel_prevent_default = True,
        touch_prevent_default = True,
        touch_stop_propagation = True):
    full_deps = deps + [
        Label("//core:sgl-core"),
        Label("//backends/html5:sgl-html5"),
        Label("@maven//:org_scala_js_scalajs_dom_sjs1_3"),
    ]

    html5_opengl_main(
        name = name + "_Main",
        package = package,
        main_class = main_class,
        export_name = export_name,
        core_abstract_class = core_abstract_class,
        assets_serving_root = static_folder,
        canvas_width = canvas_width,
        canvas_height = canvas_height,
        target_fps = target_fps,
        extra_mixins = extra_mixins,
        keyboard_capture = keyboard_capture,
        keyboard_prevent_default = keyboard_prevent_default,
        mouse_prevent_default = mouse_prevent_default,
        wheel_prevent_default = wheel_prevent_default,
        touch_prevent_default = touch_prevent_default,
        touch_stop_propagation = touch_stop_propagation,
    )

    scalajs_module(
        name = name + "_indexjs",
        srcs = [":" + name + "_Main"],
        deps = full_deps,
        output_name = output_name,
    )

    run_server(
        name = name,
        scalajs_module = ":" + name + "_indexjs",
        main_class = export_name,
        static_files = assets,
        static_strip_prefix = assets_strip_prefix,
        static_folder = static_folder,
        html_output_name = html_output_name,
    )
