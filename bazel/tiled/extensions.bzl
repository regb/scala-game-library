"""Bazel extension for downloading Tiled AppImage."""

def _tiled_download_impl(repository_ctx):
    """Implementation for downloading Tiled AppImage and extracting it."""
    url = "https://github.com/mapeditor/tiled/releases/download/v1.11.2/Tiled-1.11.2_Linux_Qt-6_x86_64.AppImage"

    repository_ctx.download(
        url = url,
        output = "tiled.appimage",
        sha256 = "64b2b3181af5611c63b9f59674083011d5e632aacb75b2ecec1e9ce8fd30d3ad",
        executable = True,
    )

    # Extract the AppImage contents to avoid FUSE issues in sandbox.
    result = repository_ctx.execute(["./tiled.appimage", "--appimage-extract"])
    if result.return_code != 0:
        fail("Failed to extract AppImage: {}".format(result.stderr))

    # Create a wrapper script that runs Tiled.
    repository_ctx.file("tiled_wrapper.sh", """#!/bin/bash
set -e

export APPDIR="$(cd "$(dirname "$0")" && pwd)/squashfs-root"

# Set up library paths
export LD_LIBRARY_PATH="$APPDIR/usr/lib:$APPDIR/usr/lib/x86_64-linux-gnu:${LD_LIBRARY_PATH:-}"

# This AppImage only includes Qt's xcb plugin, so use a temporary X server.
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export QT_PLUGIN_PATH="$APPDIR/usr/lib/qt6/plugins:${QT_PLUGIN_PATH:-}"
if ! command -v xvfb-run >/dev/null 2>&1; then
    echo "Tiled requires xvfb-run on Linux" >&2
    exit 1
fi
exec xvfb-run -a "$APPDIR/usr/bin/tiled" "$@"
""", executable = True)

    repository_ctx.file("BUILD", """
exports_files(["tiled_wrapper.sh"])

filegroup(
    name = "tiled_extracted",
    srcs = glob(["squashfs-root/**/*"], allow_empty = False),
    visibility = ["//visibility:public"],
)

""")

_tiled_download = repository_rule(
    implementation = _tiled_download_impl,
    doc = "Downloads Tiled AppImage from GitHub releases",
)

def _tiled_extension_impl(module_ctx):
    """Implementation for the tiled extension."""
    _tiled_download(name = "tiled_tool")

tiled_extension = module_extension(
    implementation = _tiled_extension_impl,
    doc = "Extension to download Tiled tool",
)
