"""Downloads the Chrome that browser tests run in."""

load(":versions.bzl", "CHROME_SHA256", "CHROME_VERSION", "DEBIAN_PACKAGES", "DEBIAN_SNAPSHOT")

_BUILD = """
package(default_visibility = ["//visibility:public"])

filegroup(
    name = "chrome",
    srcs = glob(["chrome/**"]),
)

filegroup(
    name = "libraries",
    srcs = glob([
        "root/lib/x86_64-linux-gnu/*.so*",
        "root/usr/lib/x86_64-linux-gnu/*.so*",
    ]),
)

filegroup(
    name = "fonts",
    srcs = glob(["root/usr/share/fonts/truetype/**/*.ttf"]),
)

filegroup(
    name = "licenses",
    srcs = glob(["root/usr/share/doc/*/copyright"]),
)
"""

def _chrome_linux64_impl(ctx):
    ctx.download_and_extract(
        url = "https://storage.googleapis.com/chrome-for-testing-public/{0}/linux64/chrome-headless-shell-linux64.zip".format(CHROME_VERSION),
        sha256 = CHROME_SHA256,
        output = "chrome",
        strip_prefix = "chrome-headless-shell-linux64",
    )
    for path, sha256 in DEBIAN_PACKAGES.items():
        package = "packages/" + path.split("/")[-1]
        ctx.download(
            url = "https://snapshot.debian.org/archive/debian/{0}/{1}".format(DEBIAN_SNAPSHOT, path),
            sha256 = sha256,
            output = package,
        )
        ctx.extract(package, output = package + ".contents")
        ctx.extract(package + ".contents/data.tar.xz", output = "root")
    ctx.delete("packages")
    ctx.file("BUILD.bazel", _BUILD)
    return ctx.repo_metadata(reproducible = True)

_chrome_linux64 = repository_rule(
    implementation = _chrome_linux64_impl,
    doc = "chrome-headless-shell, and the Debian libraries it needs.",
)

def _chrome_impl(_ctx):
    _chrome_linux64(name = "sgl_chrome_linux64")

chrome = module_extension(implementation = _chrome_impl)
