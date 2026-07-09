load("//bazel/sgl:assets.bzl", "SglAssetsInfo")

_DENSITY_SCALE = {
    "ldpi": 0.75,
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

def _map_output(src_file, output_prefix):
    base = src_file.basename
    for ext in [".tmj", ".tmx", ".json"]:
        if base.endswith(ext):
            base = base[:-len(ext)]
            break
    return output_prefix + "/" + base + ".json"

def _tiled_export_impl(ctx):
    """Raw-ish Tiled export. Kept for simple/manual projects."""
    output_files = []
    for src_file in ctx.files.srcs:
        output_file = ctx.actions.declare_file(_map_output(src_file, ctx.attr.output_prefix))
        output_files.append(output_file)
        ctx.actions.run_shell(
            inputs = [src_file] + ctx.files.tilesets + ctx.files.assets,
            outputs = [output_file],
            command = "TILED=${{TILED:-/home/regb/bin/tiled}}; xvfb-run -a $TILED --export-map --embed-tilesets --detach-templates --resolve-types-and-properties json {} {}".format(
                src_file.path, output_file.path,
            ),
            mnemonic = "TiledExport",
            progress_message = "Exporting Tiled map %s to %s" % (src_file.short_path, output_file.short_path),
            execution_requirements = {"local": "1", "no-sandbox": "1"},
        )
    return [DefaultInfo(files = depset(output_files))]

tiled_export = rule(
    implementation = _tiled_export_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".json", ".tmx", ".tmj"], mandatory = True),
        "tilesets": attr.label_list(allow_files = [".json", ".tmx", ".tsx", ".tsj"], mandatory = False),
        "assets": attr.label_list(allow_files = True, mandatory = False),
        "output_prefix": attr.string(mandatory = True),
    },
    doc = "Processes Tiled map files and exports them with embedded tilesets and resolved properties",
)

def _sgl_tiled_import_impl(ctx):
    source_scale = _DENSITY_SCALE[ctx.attr.source_density]
    outputs = []
    image_output_lines = []
    image_outputs = []
    for image in ctx.files.images:
        if not image.short_path.startswith(ctx.attr.project_root + "/"):
            fail("Tiled image is outside project_root '{}': {}".format(ctx.attr.project_root, image.short_path))
        rel = image.short_path[len(ctx.attr.project_root) + 1:]
        for density in ctx.attr.target_densities:
            out = ctx.actions.declare_file(ctx.attr.output_prefix + "/drawable-" + density + "/" + rel)
            outputs.append(out)
            image_outputs.append(out)
            image_output_lines.append("{}|{}|{}".format(image.short_path, density, out.path))

    manifest_file = ctx.actions.declare_file(ctx.attr.output_prefix + "/assets_manifest.json")
    outputs.append(manifest_file)

    map_lines = []
    map_outputs = []
    for src_file in ctx.files.maps:
        output_file = ctx.actions.declare_file(_map_output(src_file, ctx.attr.output_prefix + "/levels"))
        outputs.append(output_file)
        map_outputs.append(output_file)
        map_lines.append("{}|{}".format(src_file.path, output_file.path))

    ctx.actions.run_shell(
        inputs = ctx.files.maps + ctx.files.tilesets + ctx.files.images,
        outputs = outputs,
        command = """set -e
TILED=${{TILED:-/home/regb/bin/tiled}}
python3 - <<'PY'
import json, os, posixpath, shutil, subprocess, tempfile
project_root = '{project_root}'
source_density = '{source_density}'
source_scale = {source_scale}
tile_extrude = {tile_extrude}
source_extrude_px = int(round(source_scale)) if tile_extrude else 0
target_scales = {target_scales}
image_outputs = '''{image_outputs}'''.splitlines()
map_lines = '''{map_lines}'''.splitlines()
image_paths = '''{image_paths}'''.splitlines()
manifest_path = '{manifest_path}'
image_set = set(posixpath.normpath(p) for p in image_paths if p)
image_out = {{}}
for line in image_outputs:
    src, density, out = line.split('|')
    image_out[(src, density)] = out

def project_relative_candidate(path):
    path = posixpath.normpath(path)
    if path.startswith(project_root + '/'):
        return path
    marker = '/' + project_root + '/'
    if marker in path:
        return project_root + '/' + path.split(marker, 1)[1]
    return path

def resolve_ref(ref, out_path, map_path):
    candidates = [
        project_relative_candidate(ref),
        project_relative_candidate(posixpath.join(posixpath.dirname(out_path), ref)),
        project_relative_candidate(posixpath.join(posixpath.dirname(map_path), ref)),
    ]
    for resolved in candidates:
        if resolved.startswith(project_root + '/') and resolved in image_set:
            return resolved
    raise Exception('Tiled image reference could not be mapped to a declared image under %s: %s; candidates=%s; declared=%s' % (project_root, ref, candidates, sorted(image_set)))

def sgl_uri(resolved):
    rel = resolved[len(project_root) + 1:]
    asset_id = rel.replace('/', '_').replace('.', '_').replace('-', '_')
    return 'asset://drawable/' + asset_id

def convert_resize(src, density, out):
    os.makedirs(os.path.dirname(out), exist_ok=True)
    if density == source_density:
        shutil.copyfile(src, out)
    else:
        percent = int((target_scales[density] / source_scale) * 100)
        subprocess.check_call(['convert', src, '-resize', str(percent) + '%', out])

def extrude_tileset(src, out, tw, th, columns, tilecount, margin, spacing, extrude):
    os.makedirs(os.path.dirname(out), exist_ok=True)
    if extrude <= 0:
        shutil.copyfile(src, out); return
    rows = (tilecount + columns - 1) // columns
    with tempfile.TemporaryDirectory() as td:
        padded = []
        for i in range(tilecount):
            row, col = divmod(i, columns)
            x = margin + col * (tw + spacing)
            y = margin + row * (th + spacing)
            crop = os.path.join(td, 'tile_%04d.png' % i)
            pad = os.path.join(td, 'pad_%04d.png' % i)
            subprocess.check_call(['convert', src, '-crop', '%dx%d+%d+%d' % (tw, th, x, y), '+repage', crop])
            subprocess.check_call(['convert', crop, '-virtual-pixel', 'edge', '-define', 'distort:viewport=%dx%d-%d-%d' % (tw + 2*extrude, th + 2*extrude, extrude, extrude), '-distort', 'SRT', '0', pad])
            padded.append(pad)
        subprocess.check_call(['montage'] + padded + ['-tile', str(columns) + 'x', '-geometry', '+0+0', '-background', 'none', out])

# Export maps and collect which images are tilesets plus their source-density metadata.
tileset_meta = {{}}
for line in map_lines:
    map_path, out_path = line.split('|')
    subprocess.check_call(['xvfb-run', '-a', os.environ.get('TILED', '/home/regb/bin/tiled'), '--export-map', '--embed-tilesets', '--detach-templates', '--resolve-types-and-properties', 'json', map_path, out_path])
    with open(out_path) as f:
        data = json.load(f)
    for ts in data.get('tilesets', []):
        if isinstance(ts.get('image'), str):
            resolved = resolve_ref(ts['image'], out_path, map_path)
            tileset_meta[resolved] = dict(
                tilewidth=ts['tilewidth'], tileheight=ts['tileheight'], columns=ts['columns'], tilecount=ts['tilecount'], margin=ts.get('margin', 0), spacing=ts.get('spacing', 0)
            )
            ts['image'] = sgl_uri(resolved)
            e = source_extrude_px
            if e > 0:
                ts['margin'] = ts.get('margin', 0) + e
                ts['spacing'] = ts.get('spacing', 0) + 2*e
                rows = (ts['tilecount'] + ts['columns'] - 1) // ts['columns']
                ts['imagewidth'] = ts['columns'] * (ts['tilewidth'] + 2*e)
                ts['imageheight'] = rows * (ts['tileheight'] + 2*e)
    def rewrite_images(x):
        if isinstance(x, dict):
            if isinstance(x.get('image'), str) and not x['image'].startswith('sgl://') and not x['image'].startswith('asset://'):
                x['image'] = sgl_uri(resolve_ref(x['image'], out_path, map_path))
            for v in x.values(): rewrite_images(v)
        elif isinstance(x, list):
            for v in x: rewrite_images(v)
    rewrite_images(data)
    with open(out_path, 'w') as f:
        json.dump(data, f, separators=(',', ':'))

# Generate images. Tileset images are extruded at source density before downscaling.
for src in image_paths:
    if not src: continue
    meta = tileset_meta.get(src)
    source_out = image_out[(src, source_density)]
    if meta:
        extrude_tileset(src, source_out, meta['tilewidth'], meta['tileheight'], meta['columns'], meta['tilecount'], meta['margin'], meta['spacing'], source_extrude_px)
    else:
        convert_resize(src, source_density, source_out)
    for density in target_scales:
        if density == source_density or (src, density) not in image_out: continue
        convert_resize(source_out, density, image_out[(src, density)])

manifest = {{'drawables': [], 'texts': []}}
for src in image_paths:
    if not src: continue
    rel = src[len(project_root) + 1:]
    name = rel.replace('/', '_').replace('.', '_').replace('-', '_')
    variants = [{{'density': density, 'path': 'drawable-' + density + '/' + rel}} for density in target_scales]
    default_density = 'mdpi' if 'mdpi' in target_scales else next(iter(target_scales))
    manifest['drawables'].append({{'name': name, 'uri': 'asset://drawable/' + name, 'path': 'drawable-' + default_density + '/' + rel, 'variants': variants}})
for line in map_lines:
    _, out_path = line.split('|')
    base = os.path.basename(out_path)
    name = os.path.splitext(base)[0]
    manifest['texts'].append({{'name': name, 'path': 'levels/' + base}})
os.makedirs(os.path.dirname(manifest_path), exist_ok=True)
with open(manifest_path, 'w') as f:
    json.dump(manifest, f, separators=(',', ':'))
PY
""".format(
            project_root = ctx.attr.project_root,
            source_density = ctx.attr.source_density,
            source_scale = source_scale,
            tile_extrude = "True" if ctx.attr.tile_extrude else "False",
            target_scales = repr({d: _DENSITY_SCALE[d] for d in ctx.attr.target_densities}),
            image_outputs = "\n".join(image_output_lines),
            map_lines = "\n".join(map_lines),
            image_paths = "\n".join([f.short_path for f in ctx.files.images]),
            manifest_path = manifest_file.path,
        ),
        mnemonic = "SglTiledImport",
        progress_message = "Importing Tiled project assets",
        execution_requirements = {"local": "1", "no-sandbox": "1"},
    )

    return [
        DefaultInfo(files = depset(outputs)),
        SglAssetsInfo(
            files = depset(outputs),
            drawables = depset(image_outputs),
            raw_images = depset([]),
            binaries = depset([]),
            texts = depset(map_outputs),
            audio = depset([]),
            fonts = depset([]),
            manifests = depset([manifest_file]),
        ),
    ]

sgl_tiled_import = rule(
    implementation = _sgl_tiled_import_impl,
    attrs = {
        "project_root": attr.string(mandatory = True),
        "maps": attr.label_list(allow_files = [".json", ".tmx", ".tmj"], mandatory = True),
        "tilesets": attr.label_list(allow_files = [".json", ".tmx", ".tsx", ".tsj"], mandatory = False),
        "images": attr.label_list(allow_files = [".png", ".jpg", ".jpeg", ".webp"], mandatory = True),
        "source_density": attr.string(mandatory = True, values = _DENSITY_SCALE.keys()),
        "target_densities": attr.string_list(mandatory = True, allow_empty = False),
        "output_prefix": attr.string(default = "assets"),
        "tile_extrude": attr.bool(default = False, doc = "When enabled, extrude tileset atlas tiles by approximately 1 mdpi pixel, scaled to source_density."),
    },
    doc = "Imports a Tiled project into SGL runtime assets, rewriting image paths to asset://drawable/<id> URIs and generating density buckets.",
)
