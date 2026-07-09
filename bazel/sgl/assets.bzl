SglAssetsInfo = provider(fields = ["files", "drawables", "raw_images", "binaries", "texts", "audio", "fonts", "manifests"])

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

def _copy_typed_assets(ctx, resource_prefix):
    outputs = []
    lines = []
    for src in ctx.files.srcs:
        rel = _relative_resource_path(src, ctx.attr.strip_prefix)
        name = _asset_name_from_path(src, ctx.attr.strip_prefix)
        resource_path = resource_prefix.rstrip("/") + "/" + rel
        out = ctx.actions.declare_file(ctx.attr.output_prefix.rstrip("/") + "/" + resource_path)
        outputs.append(out)
        ctx.actions.run_shell(
            inputs = [src],
            outputs = [out],
            command = "mkdir -p $(dirname '{out}') && cp '{src}' '{out}'".format(src = src.path, out = out.path),
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
    },
)

def _sgl_drawable_assets_impl(ctx):
    source_scale = _DENSITY_SCALE[ctx.attr.source_density]
    outputs = []
    lines = []
    for src in ctx.files.srcs:
        name = _asset_name_from_path(src, ctx.attr.strip_prefix)
        ext = src.basename.split(".")[-1]
        for density in ctx.attr.target_densities:
            out = ctx.actions.declare_file(ctx.attr.output_prefix + "/drawable-" + density + "/" + name + "." + ext)
            outputs.append(out)
            if density == ctx.attr.source_density:
                cmd = "cp '{}' '{}'".format(src.path, out.path)
            else:
                pct = int((_DENSITY_SCALE[density] / source_scale) * 100)
                cmd = "convert '{}' -resize {}% '{}'".format(src.path, pct, out.path)
            ctx.actions.run_shell(inputs = [src], outputs = [out], command = cmd, mnemonic = "SglDrawableAsset")
        variants = [density + "=" + "drawable-" + density + "/" + name + "." + ext for density in ctx.attr.target_densities]
        default_path = "drawable-mdpi/" + name + "." + ext if "mdpi" in ctx.attr.target_densities else "drawable-" + ctx.attr.target_densities[0] + "/" + name + "." + ext
        lines.append("{}|{}|{}|{}".format(name, "asset://drawable/" + name, default_path, ";".join(variants)))
    manifest = ctx.actions.declare_file(ctx.attr.output_prefix + "/" + ctx.attr.name + "_assets_manifest.json")
    ctx.actions.write(manifest, _manifest_json_from_drawable_lines(lines))
    all_files = depset(outputs + [manifest])
    return [DefaultInfo(files = all_files), SglAssetsInfo(files = all_files, drawables = depset(outputs), raw_images = depset([]), binaries = depset([]), texts = depset([]), audio = depset([]), fonts = depset([]), manifests = depset([manifest]))]

sgl_drawable_assets = rule(
    implementation = _sgl_drawable_assets_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"], mandatory = True),
        "strip_prefix": attr.string(default = ""),
        "source_density": attr.string(mandatory = True, values = _DENSITY_SCALE.keys()),
        "target_densities": attr.string_list(mandatory = True, allow_empty = False),
        "output_prefix": attr.string(default = "assets"),
    },
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

def _json_obj(items):
    return "{" + ",".join(["\"{}\":\"{}\"".format(k, v) for k, v in items]) + "}"

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
python3 - <<'PY'
import json, os, re
out='{out}'; package='{package}'
manifest_paths='''{manifest_paths}'''.splitlines()
def ident(s):
  parts=[p for p in re.split(r'[^0-9A-Za-z]+', s) if p]
  if not parts: return 'asset'
  n=parts[0][0].lower()+parts[0][1:]
  for p in parts[1:]: n += p[0].upper()+p[1:]
  return 'asset'+n if n[0].isdigit() else n
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
def uniq(xs):
  seen=set(); r=[]
  for x in xs:
    if x['name'] not in seen: seen.add(x['name']); r.append(x)
  return r
drawables=uniq(drawables); raw=uniq(raw); binary=uniq(binary); texts=uniq(texts); audio=uniq(audio); fonts=uniq(fonts)
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
""".format(out=out.path, package=ctx.attr.package, manifest_paths="\n".join([m.path for m in manifests])),
        mnemonic = "SglAssetsProvider",
    )
    return [DefaultInfo(files = depset([out]))]

sgl_assets_provider = rule(
    implementation = _sgl_assets_provider_impl,
    attrs = {"package": attr.string(mandatory = True), "asset_groups": attr.label_list(providers = [SglAssetsInfo], default = [])},
)
