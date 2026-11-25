#!/usr/bin/env python3
"""Writes versions.bzl: run it to update the bundled Chrome.

    resolve.py <chrome version> <debian snapshot date, like 20260901T000000Z>

It follows the libraries Chrome names to their Debian packages. It needs curl,
unzip, dpkg-deb and readelf.
"""

import hashlib
import lzma
import os
import re
import subprocess
import sys
import tempfile

# The package of each library. The script names the ones that are missing here.
PACKAGES = {
    "libX11.so.6": "libx11-6",
    "libXau.so.6": "libxau6",
    "libXcomposite.so.1": "libxcomposite1",
    "libXdamage.so.1": "libxdamage1",
    "libXdmcp.so.6": "libxdmcp6",
    "libXext.so.6": "libxext6",
    "libXfixes.so.3": "libxfixes3",
    "libXi.so.6": "libxi6",
    "libXrandr.so.2": "libxrandr2",
    "libXrender.so.1": "libxrender1",
    "libasound.so.2": "libasound2",
    "libatk-1.0.so.0": "libatk1.0-0",
    "libatk-bridge-2.0.so.0": "libatk-bridge2.0-0",
    "libatspi.so.0": "libatspi2.0-0",
    "libblkid.so.1": "libblkid1",
    "libbsd.so.0": "libbsd0",
    "libcap.so.2": "libcap2",
    "libdbus-1.so.3": "libdbus-1-3",
    "libdrm.so.2": "libdrm2",
    "libexpat.so.1": "libexpat1",
    "libffi.so.8": "libffi8",
    "libgbm.so.1": "libgbm1",
    "libgcrypt.so.20": "libgcrypt20",
    "libgio-2.0.so.0": "libglib2.0-0",
    "libglib-2.0.so.0": "libglib2.0-0",
    "libgmodule-2.0.so.0": "libglib2.0-0",
    "libgobject-2.0.so.0": "libglib2.0-0",
    "libgpg-error.so.0": "libgpg-error0",
    "liblz4.so.1": "liblz4-1",
    "liblzma.so.5": "liblzma5",
    "libmd.so.0": "libmd0",
    "libmount.so.1": "libmount1",
    "libnspr4.so": "libnspr4",
    "libnss3.so": "libnss3",
    "libnssutil3.so": "libnss3",
    "libpcre2-8.so.0": "libpcre2-8-0",
    "libplc4.so": "libnspr4",
    "libplds4.so": "libnspr4",
    "libselinux.so.1": "libselinux1",
    "libstdc++.so.6": "libstdc++6",
    "libsystemd.so.0": "libsystemd0",
    "libudev.so.1": "libudev1",
    "libwayland-server.so.0": "libwayland-server0",
    "libxcb-randr.so.0": "libxcb-randr0",
    "libxcb.so.1": "libxcb1",
    "libxkbcommon.so.0": "libxkbcommon0",
    "libz.so.1": "zlib1g",
    "libzstd.so.1": "libzstd1",
}

# Not found by following libraries.
ROOTS = ["libc6", "libgcc-s1", "fonts-dejavu-core"]
GLIBC = re.compile(r"(lib(c|m|dl|pthread|rt|resolv)\.so|ld-linux).*")
DIRECTORIES = ["root/lib/x86_64-linux-gnu", "root/usr/lib/x86_64-linux-gnu"]


def download(url, path):
    subprocess.run(["curl", "-sfL", "--retry", "5", "-o", path, url], check=True)
    with open(path, "rb") as f:
        return hashlib.sha256(f.read()).hexdigest()


def needed(path):
    output = subprocess.run(["readelf", "-d", path], capture_output=True, text=True).stdout
    return re.findall(r"NEEDED.*\[(.*?)\]", output)


def main(chrome_version, snapshot):
    here = os.path.dirname(os.path.abspath(__file__))
    with tempfile.TemporaryDirectory() as directory:
        os.chdir(directory)
        chrome_sha256 = download(
            f"https://storage.googleapis.com/chrome-for-testing-public/{chrome_version}"
            "/linux64/chrome-headless-shell-linux64.zip", "chrome.zip")
        subprocess.run(["unzip", "-qo", "chrome.zip"], check=True)

        base = f"https://snapshot.debian.org/archive/debian/{snapshot}/"
        download(base + "dists/bookworm/main/binary-amd64/Packages.xz", "Packages.xz")
        index = {}
        with lzma.open("Packages.xz", "rt") as f:
            for entry in f.read().split("\n\n"):
                fields = dict(re.findall(r"^(\S+): (.*)$", entry, re.M))
                if "Package" in fields:
                    index[fields["Package"]] = fields

        used = {}

        def use(package):
            if package not in used:
                fields = index[package]
                used[package] = fields
                if download(base + fields["Filename"], "package.deb") != fields["SHA256"]:
                    raise SystemExit(f"{package} is not what the index says")
                subprocess.run(["dpkg-deb", "-x", "package.deb", "root"], check=True)

        def find(library):
            paths = [os.path.join(d, library) for d in DIRECTORIES]
            return next((path for path in paths if os.path.exists(path)), None)

        for package in ROOTS:
            use(package)
        chrome = "chrome-headless-shell-linux64"
        todo = [os.path.join(chrome, f) for f in os.listdir(chrome) if f == "chrome-headless-shell" or ".so" in f]
        seen = set()
        while todo:
            for library in needed(todo.pop()):
                if library in seen or GLIBC.fullmatch(library):
                    continue
                seen.add(library)
                if not find(library):
                    if library not in PACKAGES:
                        raise SystemExit(f"Add the package of {library} to PACKAGES")
                    use(PACKAGES[library])
                if not find(library):
                    raise SystemExit(f"{PACKAGES[library]} does not have {library}")
                todo.append(find(library))

        lines = [
            '"""What the bundled Chrome is made of. `resolve.py` writes this file."""', "",
            "# Chrome for Testing, https://googlechromelabs.github.io/chrome-for-testing/",
            f'CHROME_VERSION = "{chrome_version}"',
            f'CHROME_SHA256 = "{chrome_sha256}"', "",
            "# Debian bookworm, as of this date.",
            f'DEBIAN_SNAPSHOT = "{snapshot}"', "",
            "# The libraries Chrome needs, with theirs down to glibc, and a font.",
            "DEBIAN_PACKAGES = {",
        ]
        for fields in sorted(used.values(), key=lambda f: f["Filename"]):
            lines.append(f'    "{fields["Filename"]}": "{fields["SHA256"]}",')
        lines.append("}")
        with open(os.path.join(here, "versions.bzl"), "w") as f:
            f.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    main(sys.argv[1], sys.argv[2])
