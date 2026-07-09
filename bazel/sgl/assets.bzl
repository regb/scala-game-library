SglAssetsInfo = provider(fields = ["files", "drawables", "raw_images", "binaries", "texts", "audio", "fonts", "manifests"])

_DENSITIES = ["ldpi", "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"]
_DENSITY_SCALE = {"ldpi": 0.75, "mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}

def _asset_name_from_path(f, strip_prefix):
    p = f.short_path
    if strip_prefix:
        prefix = strip_prefix.rstrip("/") + "/"
        if not p.startswith(prefix):
            fail("Asset {} is not under strip_prefix {}".format(p, strip_prefix))
        p = p[len(prefix):]
    if "." in p:
        p = ".".join(p.split(".")[:-1])
    return p.replace("/", "_").replace("-", "_").replace(".", "_")

def _relative_resource_path(f, strip_prefix):
    p = f.short_path
    if strip_prefix:
        prefix = strip_prefix.rstrip("/") + "/"
        if not p.startswith(prefix):
            fail("Resource {} is not under strip_prefix {}".format(p, strip_prefix))
        p = p[len(prefix):]
    return p

def _validate_manifest_field(value, source):
    for separator in ["|", "\n", "\r"]:
        if separator in value:
            fail("Asset path contains unsupported manifest separator in %s: %s" % (source, value))

def _copy_typed_assets(ctx, resource_prefix):
    outputs = []
    lines = []
    for src in ctx.files.srcs:
        rel = _relative_resource_path(src, ctx.attr.strip_prefix)
        name = _asset_name_from_path(src, ctx.attr.strip_prefix)
        _validate_manifest_field(rel, src.short_path)
        resource_path = resource_prefix.rstrip("/") + "/" + rel
        out = ctx.actions.declare_file(ctx.attr.output_prefix.rstrip("/") + "/" + resource_path)
        outputs.append(out)
        ctx.actions.run(
            executable = ctx.executable._image_tool,
            inputs = [src],
            outputs = [out],
            arguments = ["copy", src.path, out.path],
            mnemonic = "SglTypedAsset",
        )
        lines.append("{}|{}".format(name, resource_path))
    return outputs, lines

def _sgl_raw_image_assets_impl(ctx):
    outputs, lines = _copy_typed_assets(ctx, "raw-image")
    manifest = ctx.actions.declare_file(ctx.attr.name + "/assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json(drawables = [], raw_images = lines, binaries = [], texts = [], audio = []))
    provider_files = depset(outputs + [manifest])
    return [DefaultInfo(files = depset(outputs)), SglAssetsInfo(files = provider_files, drawables = depset([]), raw_images = depset(outputs), binaries = depset([]), texts = depset([]), audio = depset([]), fonts = depset([]), manifests = depset([manifest]))]

sgl_raw_image_assets = rule(
    implementation = _sgl_raw_image_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"], mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def _sgl_text_assets_impl(ctx):
    outputs, lines = _copy_typed_assets(ctx, "text")
    manifest = ctx.actions.declare_file(ctx.attr.name + "/assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json(drawables = [], raw_images = [], binaries = [], texts = lines, audio = []))
    provider_files = depset(outputs + [manifest])
    return [DefaultInfo(files = depset(outputs)), SglAssetsInfo(files = provider_files, drawables = depset([]), raw_images = depset([]), binaries = depset([]), texts = depset(outputs), audio = depset([]), fonts = depset([]), manifests = depset([manifest]))]

sgl_text_assets = rule(
    implementation = _sgl_text_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = True, mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def _sgl_binary_assets_impl(ctx):
    outputs, lines = _copy_typed_assets(ctx, "binary")
    manifest = ctx.actions.declare_file(ctx.attr.name + "/assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json(drawables = [], raw_images = [], binaries = lines, texts = [], audio = []))
    provider_files = depset(outputs + [manifest])
    return [DefaultInfo(files = depset(outputs)), SglAssetsInfo(files = provider_files, drawables = depset([]), raw_images = depset([]), binaries = depset(outputs), texts = depset([]), audio = depset([]), fonts = depset([]), manifests = depset([manifest]))]

sgl_binary_assets = rule(
    implementation = _sgl_binary_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = True, mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def _sgl_audio_assets_impl(ctx):
    outputs, lines = _copy_typed_assets(ctx, "audio")
    manifest = ctx.actions.declare_file(ctx.attr.name + "/assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json(drawables = [], raw_images = [], binaries = [], texts = [], audio = lines))
    provider_files = depset(outputs + [manifest])
    return [DefaultInfo(files = depset(outputs)), SglAssetsInfo(files = provider_files, drawables = depset([]), raw_images = depset([]), binaries = depset([]), texts = depset([]), audio = depset(outputs), fonts = depset([]), manifests = depset([manifest]))]

sgl_audio_assets = rule(
    implementation = _sgl_audio_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".wav", ".ogg", ".mp3"], mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def _sgl_font_assets_impl(ctx):
    outputs, lines = _copy_typed_assets(ctx, "font")
    manifest = ctx.actions.declare_file(ctx.attr.name + "/assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json(drawables = [], raw_images = [], binaries = [], texts = [], audio = [], fonts = lines))
    provider_files = depset(outputs + [manifest])
    return [DefaultInfo(files = depset(outputs)), SglAssetsInfo(files = provider_files, drawables = depset([]), raw_images = depset([]), binaries = depset([]), texts = depset([]), audio = depset([]), fonts = depset(outputs), manifests = depset([manifest]))]

sgl_font_assets = rule(
    implementation = _sgl_font_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".ttf", ".otf", ".woff", ".woff2"], mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def _validate_drawable_name(name, source):
    _validate_manifest_field(name, source)
    if ";" in name:
        fail("Drawable asset path contains unsupported manifest separator: %s" % source)

def _drawable_result(ctx, outputs, lines):
    manifest = ctx.actions.declare_file(ctx.attr.output_prefix + "/" + ctx.attr.name + "_assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json_from_drawable_lines(lines))
    all_files = depset(outputs + [manifest])
    return [DefaultInfo(files = all_files), SglAssetsInfo(files = all_files, drawables = depset(outputs), raw_images = depset([]), binaries = depset([]), texts = depset([]), audio = depset([]), fonts = depset([]), manifests = depset([manifest]))]

def _copy_supplied_drawable_variants(ctx):
    sources_by_density = [
        ("ldpi", ctx.files.ldpi_srcs),
        ("mdpi", ctx.files.mdpi_srcs),
        ("hdpi", ctx.files.hdpi_srcs),
        ("xhdpi", ctx.files.xhdpi_srcs),
        ("xxhdpi", ctx.files.xxhdpi_srcs),
        ("xxxhdpi", ctx.files.xxxhdpi_srcs),
    ]
    assets = {}
    for density, sources in sources_by_density:
        strip_prefix = ctx.attr.density_strip_prefixes.get(density, ctx.attr.strip_prefix)
        for src in sources:
            name = _asset_name_from_path(src, strip_prefix)
            _validate_drawable_name(name, src.short_path)
            variants = assets.setdefault(name, {})
            if density in variants:
                fail("Drawable asset %s has more than one %s variant: %s and %s" % (name, density, variants[density][0].short_path, src.short_path))
            variants[density] = (src, src.basename.split(".")[-1])

    outputs = []
    lines = []
    for name in sorted(assets.keys()):
        supplied = assets[name]
        densities = [density for density in _DENSITIES if density in supplied]
        variant_lines = []
        paths = {}
        for density in densities:
            src, ext = supplied[density]
            resource_path = "drawable-" + density + "/" + name + "." + ext
            out = ctx.actions.declare_file(ctx.attr.output_prefix + "/" + resource_path)
            outputs.append(out)
            paths[density] = resource_path
            ctx.actions.run(
                executable = ctx.executable._image_tool,
                inputs = [src],
                outputs = [out],
                arguments = ["copy", src.path, out.path],
                mnemonic = "SglDrawableAsset",
            )
            variant_lines.append(density + "=" + resource_path)
        default_density = "mdpi" if "mdpi" in supplied else densities[0]
        lines.append("{}|{}|{}|{}".format(name, "asset://drawable/" + name, paths[default_density], ";".join(variant_lines)))
    return _drawable_result(ctx, outputs, lines)

def _resize_drawable_variants(ctx):
    source_scale = _DENSITY_SCALE[ctx.attr.source_density]
    outputs = []
    lines = []
    for src in ctx.files.srcs:
        name = _asset_name_from_path(src, ctx.attr.strip_prefix)
        _validate_drawable_name(name, src.short_path)
        ext = src.basename.split(".")[-1]
        for density in ctx.attr.target_densities:
            out = ctx.actions.declare_file(ctx.attr.output_prefix + "/drawable-" + density + "/" + name + "." + ext)
            outputs.append(out)
            if density == ctx.attr.source_density:
                ctx.actions.run(
                    executable = ctx.executable._image_tool,
                    inputs = [src],
                    outputs = [out],
                    arguments = ["copy", src.path, out.path],
                    mnemonic = "SglDrawableAsset",
                )
            else:
                if ext.lower() == "webp":
                    fail("Drawable WebP assets cannot be density-resized; use PNG/JPEG or only request the source density: %s" % src.short_path)
                pct = int((_DENSITY_SCALE[density] / source_scale) * 100)
                ctx.actions.run(
                    executable = ctx.executable._image_tool,
                    inputs = [src],
                    outputs = [out],
                    arguments = ["resize", src.path, str(pct), out.path],
                    mnemonic = "SglDrawableAsset",
                )
        variants = [density + "=" + "drawable-" + density + "/" + name + "." + ext for density in ctx.attr.target_densities]
        default_path = "drawable-mdpi/" + name + "." + ext if "mdpi" in ctx.attr.target_densities else "drawable-" + ctx.attr.target_densities[0] + "/" + name + "." + ext
        lines.append("{}|{}|{}|{}".format(name, "asset://drawable/" + name, default_path, ";".join(variants)))
    return _drawable_result(ctx, outputs, lines)

def _sgl_drawable_assets_impl(ctx):
    if ctx.attr.use_supplied_densities:
        return _copy_supplied_drawable_variants(ctx)
    return _resize_drawable_variants(ctx)

_sgl_drawable_assets = rule(
    implementation = _sgl_drawable_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "strip_prefix": attr.string(default = ""),
        "source_density": attr.string(default = "mdpi", values = _DENSITY_SCALE.keys()),
        "target_densities": attr.string_list(default = ["mdpi"], allow_empty = False),
        "use_supplied_densities": attr.bool(default = False),
        "density_strip_prefixes": attr.string_dict(),
        "ldpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "mdpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "hdpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "xhdpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "xxhdpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "xxxhdpi_srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"]),
        "output_prefix": attr.string(default = "assets"),
        "_image_tool": attr.label(default = "//bazel/tools:image_asset_tool", executable = True, cfg = "exec"),
    },
)

def sgl_drawable_assets(name, srcs = None, strip_prefix = "", source_density = None, target_densities = None, density_srcs = None, density_strip_prefixes = None, output_prefix = "assets", **kwargs):
    """Declares drawable assets from one resizable source density or supplied density variants."""
    if density_srcs != None:
        if srcs != None or source_density != None or target_densities != None:
            fail("density_srcs cannot be combined with srcs, source_density, or target_densities")
        if not density_srcs:
            fail("density_srcs must contain at least one density")
        unknown = [density for density in density_srcs.keys() if density not in _DENSITY_SCALE]
        if unknown:
            fail("Unknown drawable densities: %s" % ", ".join(sorted(unknown)))
        prefixes = density_strip_prefixes or {}
        unknown_prefixes = [density for density in prefixes.keys() if density not in density_srcs]
        if unknown_prefixes:
            fail("density_strip_prefixes contains densities absent from density_srcs: %s" % ", ".join(sorted(unknown_prefixes)))
        attrs = {}
        for density in _DENSITIES:
            attrs[density + "_srcs"] = density_srcs.get(density, [])
        _sgl_drawable_assets(
            name = name,
            strip_prefix = strip_prefix,
            use_supplied_densities = True,
            density_strip_prefixes = prefixes,
            output_prefix = output_prefix,
            **dict(attrs, **kwargs)
        )
    else:
        if srcs == None:
            fail("srcs is required when density_srcs is not specified")
        if source_density == None:
            fail("source_density is required when density_srcs is not specified")
        if target_densities == None:
            fail("target_densities is required when density_srcs is not specified")
        _sgl_drawable_assets(
            name = name,
            srcs = srcs,
            strip_prefix = strip_prefix,
            source_density = source_density,
            target_densities = target_densities,
            output_prefix = output_prefix,
            **kwargs
        )

def _manifest_json(drawables, raw_images, binaries, texts, audio, fonts = []):
    # lines are name|path for raw/binary/text/audio and name|uri|path(|density=path;...) for drawables.
    parts = ["{\"drawables\":["]
    drawable_objs = []
    for l in drawables:
        fields = l.split("|")
        items = [("name", fields[0]), ("uri", fields[1]), ("path", fields[2])]
        obj = _json_obj(items)
        if len(fields) > 3 and fields[3]:
            variants = []
            for variant in fields[3].split(";"):
                if variant:
                    density_path = variant.split("=", 1)
                    variants.append(_json_obj([("density", density_path[0]), ("path", density_path[1])]))
            obj = obj[:-1] + ",\"variants\":[" + ",".join(variants) + "]}"
        drawable_objs.append(obj)
    parts.append(",".join(drawable_objs))
    parts.append("],\"rawImages\":[")
    parts.append(",".join([_json_obj([("name", l.split("|")[0]), ("path", l.split("|")[1])]) for l in raw_images]))
    parts.append("],\"binaries\":[")
    parts.append(",".join([_json_obj([("name", l.split("|")[0]), ("path", l.split("|")[1])]) for l in binaries]))
    parts.append("],\"texts\":[")
    parts.append(",".join([_json_obj([("name", l.split("|")[0]), ("path", l.split("|")[1])]) for l in texts]))
    parts.append("],\"audio\":[")
    parts.append(",".join([_json_obj([("name", l.split("|")[0]), ("path", l.split("|")[1])]) for l in audio]))
    parts.append("],\"fonts\":[")
    parts.append(",".join([_json_obj([("name", l.split("|")[0]), ("path", l.split("|")[1])]) for l in fonts]))
    parts.append("]}")
    return "".join(parts)

def _manifest_json_from_drawable_lines(lines):
    return _manifest_json(drawables = lines, raw_images = [], binaries = [], texts = [], audio = [])

def _json_string(value):
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""

def _json_obj(items):
    return "{" + ",".join([_json_string(k) + ":" + _json_string(v) for k, v in items]) + "}"

def _sgl_assets_provider_impl(ctx):
    out = ctx.actions.declare_file(ctx.attr.name + "/Assets.scala")
    manifests = []
    files = []
    for group in ctx.attr.asset_groups:
        info = group[SglAssetsInfo]
        manifests += info.manifests.to_list()
        files += info.files.to_list()
    ctx.actions.run_shell(
        inputs = files,
        outputs = [out],
        command = """set -e
python3 - "$@" <<'PY'
import json, os, re, sys
out=sys.argv[1]; package=sys.argv[2]; manifest_paths=sys.argv[3:]
scala_keywords={{
  'abstract', 'case', 'catch', 'class', 'def', 'do', 'else', 'extends', 'false',
  'final', 'finally', 'for', 'forSome', 'if', 'implicit', 'import', 'lazy', 'match',
  'new', 'null', 'object', 'override', 'package', 'private', 'protected', 'return',
  'sealed', 'super', 'this', 'throw', 'trait', 'true', 'try', 'type', 'val', 'var',
  'while', 'with', 'yield', 'enum', 'export', 'extension', 'given', 'then', 'end',
  'opaque', 'inline', 'open', 'transparent', 'infix', 'using', 'derives', 'macro',
}}
if not all(re.fullmatch(r'[A-Za-z_][0-9A-Za-z_]*', part) and part not in scala_keywords for part in package.split('.')):
  raise ValueError(f'Invalid Scala package name: {{package!r}}')
def ident(s):
  parts=[p for p in re.split(r'[^0-9A-Za-z]+', s) if p]
  if not parts: return 'asset'
  n=parts[0][0].lower()+parts[0][1:]
  for p in parts[1:]: n += p[0].upper()+p[1:]
  if n[0].isdigit(): n='asset'+n
  return n+'_' if n in scala_keywords else n
def q(s): return '"'+s.replace('\\\\','\\\\\\\\').replace('"','\\\\"')+'"'
def path_expr(path):
  e='ResourcesRoot'
  for p in [x for x in path.split('/') if x]: e += ' / '+q(p)
  return e
drawables=[]; raw=[]; binary=[]; texts=[]; audio=[]; fonts=[]
for mp in manifest_paths:
  data=json.load(open(mp))
  drawables += data.get('drawables', [])
  raw += data.get('rawImages', [])
  binary += data.get('binaries', [])
  texts += data.get('texts', [])
  audio += data.get('audio', [])
  fonts += data.get('fonts', [])
def validate_unique(kind, xs):
  names={{}}; identifiers={{}}
  for x in xs:
    name=x['name']; identifier=ident(name)
    if name in names:
      raise ValueError(f'Duplicate {{kind}} asset name {{name!r}}')
    if identifier in identifiers:
      previous=identifiers[identifier]
      raise ValueError(f'{{kind}} assets {{previous!r}} and {{name!r}} both generate Scala identifier {{identifier!r}}')
    names[name]=x
    identifiers[identifier]=name
  return xs
drawables=validate_unique('drawable', drawables)
raw=validate_unique('raw image', raw)
binary=validate_unique('binary', binary)
texts=validate_unique('text', texts)
audio=validate_unique('audio', audio)
fonts=validate_unique('font', fonts)
dm=[]; dv=[]; duc=[]
for d in drawables:
  i=ident(d['name'])
  dm.append('    def %s: DrawableAsset' % i)
  variants=d.get('variants', [dict(density='mdpi', path=d['path'])])
  variant_expr='Vector(' + ', '.join([q(v['density']) + ' -> ' + q(v['path']) for v in variants]) + ')'
  dv.append('      override val %s: DrawableAsset = AssetFactory.drawable(%s)' % (i, variant_expr))
  duc.append('    case %s => Assets.drawable.%s' % (q(d['uri']), i))
rm=[]; rv=[]
for r in raw:
  i=ident(r['name'])
  rm.append('    def %s: RawImageAsset' % i)
  rv.append('      override val %s: RawImageAsset = AssetFactory.rawImage(%s)' % (i, q(r['path'])))
bm=[]; bv=[]
for b in binary:
  i=ident(b['name'])
  bm.append('    def %s: BinaryAsset' % i)
  bv.append('      override val %s: BinaryAsset = AssetFactory.binary(%s)' % (i, q(b['path'])))
tm=[]; tv=[]
for t in texts:
  i=ident(t['name'])
  tm.append('    def %s: TextAsset' % i)
  tv.append('      override val %s: TextAsset = AssetFactory.text(%s)' % (i, q(t['path'])))
am=[]; av=[]
for a in audio:
  i=ident(a['name'])
  am.append('    def %s: AudioAsset' % i)
  av.append('      override val %s: AudioAsset = AssetFactory.audio(%s)' % (i, q(a['path'])))
fm=[]; fv=[]
for f in fonts:
  i=ident(f['name'])
  fm.append('    def %s: FontAsset' % i)
  fv.append('      override val %s: FontAsset = AssetFactory.font(%s)' % (i, q(f['path'])))
gen_package = 'sgl.generated.' + re.sub(r'[^0-9A-Za-z]+', '_', package).strip('_')
content = f'''package {{package}} {{{{

import sgl.SystemProvider
import sgl.assets._

trait Assets {{{{
  def drawable: DrawableAssets
  def rawImage: RawImageAssets
  def binary: BinaryAssets
  def text: TextAssets
  def audio: AudioAssets
  def font: FontAssets
  trait DrawableAssets {{{{
{{chr(10).join(dm)}}
  }}}}
  trait RawImageAssets {{{{
{{chr(10).join(rm)}}
  }}}}
  trait BinaryAssets {{{{
{{chr(10).join(bm)}}
  }}}}
  trait TextAssets {{{{
{{chr(10).join(tm)}}
  }}}}
  trait AudioAssets {{{{
{{chr(10).join(am)}}
  }}}}
  trait FontAssets {{{{
{{chr(10).join(fm)}}
  }}}}
}}}}
trait AssetsProvider extends AssetRuntime {{{{ self: SystemProvider =>
  def Assets: Assets
}}}}
trait GeneratedAssetsProvider extends {{gen_package}}.GeneratedAssetsProvider {{{{ self: SystemProvider => }}}}
}}}}

package {{gen_package}} {{{{
import sgl.SystemProvider
import sgl.assets._

trait GeneratedAssetsProvider extends {{package}}.AssetsProvider {{{{ self: SystemProvider =>
  final override val Assets: {{package}}.Assets = new {{package}}.Assets {{{{
    override val drawable: DrawableAssets = new DrawableAssets {{{{
{{chr(10).join(dv)}}
    }}}}
    override val rawImage: RawImageAssets = new RawImageAssets {{{{
{{chr(10).join(rv)}}
    }}}}
    override val binary: BinaryAssets = new BinaryAssets {{{{
{{chr(10).join(bv)}}
    }}}}
    override val text: TextAssets = new TextAssets {{{{
{{chr(10).join(tv)}}
    }}}}
    override val audio: AudioAssets = new AudioAssets {{{{
{{chr(10).join(av)}}
    }}}}
    override val font: FontAssets = new FontAssets {{{{
{{chr(10).join(fv)}}
    }}}}
  }}}}
  final override def drawableAssetFromUri(uri: String): DrawableAsset = uri match {{{{
{{chr(10).join(duc)}}
    case other => throw new NoSuchElementException(s"Unknown drawable asset URI $other")
  }}}}
}}}}
}}}}
'''
os.makedirs(os.path.dirname(out), exist_ok=True); open(out,'w').write(content)
PY
""".format(),
        arguments = [out.path, ctx.attr.package] + [m.path for m in manifests],
        mnemonic = "SglAssetsProvider",
    )
    return [DefaultInfo(files = depset([out]))]

sgl_assets_provider = rule(
    implementation = _sgl_assets_provider_impl,
    attrs = {"package": attr.string(mandatory = True), "asset_groups": attr.label_list(providers = [SglAssetsInfo], default = [])},
)
