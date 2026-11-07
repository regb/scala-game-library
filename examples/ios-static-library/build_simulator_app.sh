#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: $0 <output.app> [archive]" >&2
  exit 2
fi

root="$(cd "$(dirname "$0")/../.." && pwd)"
output="$1"
archive="${2:-$root/bazel-bin/examples/ios-static-library/snake_ios_simulator.a}"
header="$root/examples/ios-static-library/sgl_ios.h"
source="$root/examples/ios-static-library/IOSMetalShell.swift"
bridge="$root/backends/metal-native/metal_bridge.m"
plist="$root/examples/ios-static-library/Info.plist"
sdk="$(/usr/bin/xcrun --sdk iphonesimulator --show-sdk-path)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

if [[ ! -f "$archive" ]]; then
  echo "missing $archive; build //examples/ios-static-library:snake_ios_simulator first" >&2
  exit 1
fi

/usr/bin/xcrun --sdk iphonesimulator clang \
  -target arm64-apple-ios15.0-simulator \
  -isysroot "$sdk" \
  -fobjc-arc \
  -fmodules \
  -c "$bridge" \
  -o "$tmp/metal_bridge.o"

rm -rf "$output"
mkdir -p "$output"
/usr/bin/xcrun --sdk iphonesimulator swiftc \
  -parse-as-library \
  -target arm64-apple-ios15.0-simulator \
  -sdk "$sdk" \
  -import-objc-header "$header" \
  "$source" "$archive" "$tmp/metal_bridge.o" \
  -framework AVFoundation \
  -framework CoreGraphics \
  -framework Foundation \
  -framework ImageIO \
  -framework Metal \
  -framework MetalKit \
  -framework QuartzCore \
  -framework UIKit \
  -framework UniformTypeIdentifiers \
  -o "$output/SGLIOSSnake"
cp "$plist" "$output/Info.plist"
/usr/bin/codesign --force --sign - --timestamp=none "$output"
echo "$output"
