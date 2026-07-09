load(":core.bzl", _sgl_core_library = "sgl_core_library")
load(":desktop.bzl", _sgl_desktop_awt_app = "sgl_desktop_awt_app")
load(":html5.bzl", _sgl_html5_app = "sgl_html5_app")
load(":native.bzl", _sgl_desktop_native_app = "sgl_desktop_native_app")
load("//bazel/sgl/android:android.bzl", _sgl_android_app = "sgl_android_app")

sgl_core_library = _sgl_core_library
sgl_desktop_awt_app = _sgl_desktop_awt_app
sgl_html5_app = _sgl_html5_app
sgl_desktop_native_app = _sgl_desktop_native_app
sgl_android_app = _sgl_android_app

# Provider for sgl_assets rule
SglAssetsInfo = provider(
    "Information about SGL assets",
    fields = {
        "files": "List of asset files",
        "prefix": "Prefix path to the root of the assets"
    }
)

def _sgl_assets_impl(ctx):
    files = []
    for src in ctx.files.srcs:
        files.append(src)
    
    return [
        SglAssetsInfo(
            files = files,
            prefix = ctx.attr.prefix
        ),
        DefaultInfo(files = depset(files))
    ]

sgl_assets = rule(
    implementation = _sgl_assets_impl,
    attrs = {
        "srcs": attr.label_list(
            allow_files = True,
            doc = "List of asset files"
        ),
        "prefix": attr.string(
            doc = "Prefix path (from repo root) to the root of the assets"
        ),
    },
    doc = "Rule for packaging SGL assets"
)

def sgl_assets_folder(name, path):
    full_path = native.package_name() + "/" + path.strip("/")
    sgl_assets(
        name = name,
        srcs = native.glob([path.strip("/") + "/**"]),
        prefix = full_path
    )
