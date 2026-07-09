"""Bazel extension for downloading Tiled AppImage."""

def _tiled_download_impl(repository_ctx):
    """Implementation for downloading Tiled AppImage and extracting it."""
    url = "https://github.com/mapeditor/tiled/releases/download/v1.11.2/Tiled-1.11.2_Linux_Qt-6_x86_64.AppImage"
    
    repository_ctx.download(
        url = url,
        output = "tiled.appimage",
        sha256 = "",  # TODO: add sha
        executable = True,
    )
    
    # Extract the AppImage contents to avoid FUSE issues in sandbox
    result = repository_ctx.execute(["./tiled.appimage", "--appimage-extract"])
    if result.return_code != 0:
        fail("Failed to extract AppImage: {}".format(result.stderr))
    
    # Create a wrapper script that runs tiled
    repository_ctx.file("tiled_wrapper.sh", """#!/bin/bash
set -e

# Find the runfiles directory where Bazel puts our data files
if [[ -n "$RUNFILES_DIR" ]]; then
    RUNFILES_BASE="$RUNFILES_DIR"
elif [[ -n "$0" ]]; then
    RUNFILES_BASE="$0.runfiles"
else
    echo "Cannot find runfiles directory" >&2
    exit 1
fi

# Look for the extracted files in runfiles
REPO_NAME="sgl.bzl++tiled_extension+tiled_tool"
export APPDIR="$RUNFILES_BASE/$REPO_NAME/squashfs-root"

# Set up library paths
export LD_LIBRARY_PATH="$APPDIR/usr/lib:$APPDIR/usr/lib/x86_64-linux-gnu:${LD_LIBRARY_PATH:-}"

# Minimal Qt setup
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export QT_PLUGIN_PATH="$APPDIR/usr/lib/qt6/plugins:${QT_PLUGIN_PATH:-}"

# Use xvfb-run if available, otherwise try to run directly
if command -v xvfb-run >/dev/null 2>&1; then
    exec xvfb-run -a "$APPDIR/usr/bin/tiled" "$@"
else
    exec "$APPDIR/usr/bin/tiled" "$@"
fi
""", executable = True)
    
    repository_ctx.file("BUILD", """
load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar")
load("@bazel_tools//tools/bash/runfiles:runfiles.bzl", "sh_binary")

filegroup(
    name = "tiled_extracted",
    srcs = glob(["squashfs-root/**/*"], allow_empty = False),
    visibility = ["//visibility:public"],
)

sh_binary(
    name = "tiled", 
    srcs = ["tiled_wrapper.sh"],
    data = [":tiled_extracted"],
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
