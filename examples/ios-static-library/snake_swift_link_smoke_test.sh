#!/usr/bin/env bash
set -euo pipefail

workspace="${TEST_SRCDIR}/_main"
if [[ ! -d "$workspace" ]]; then
  workspace="${TEST_SRCDIR}/${TEST_WORKSPACE}"
fi
base="$workspace/examples/ios-static-library"
bridge="$workspace/backends/metal-native/metal_bridge.m"
header="$base/sgl_ios.h"
source="$base/IOSMetalShell.swift"
tmp="${TEST_TMPDIR}"

link_shell() {
  local sdk_name="$1"
  local target="$2"
  local archive="$3"
  local output="$4"
  local sdk
  sdk="$(/usr/bin/xcrun --sdk "$sdk_name" --show-sdk-path)"
  /usr/bin/xcrun --sdk "$sdk_name" clang \
    -target "$target" \
    -isysroot "$sdk" \
    -fobjc-arc \
    -fmodules \
    -c "$bridge" \
    -o "$tmp/metal_bridge_${sdk_name}.o"
  /usr/bin/xcrun --sdk "$sdk_name" swiftc \
    -parse-as-library \
    -target "$target" \
    -sdk "$sdk" \
    -import-objc-header "$header" \
    "$source" "$archive" "$tmp/metal_bridge_${sdk_name}.o" \
    -framework AVFoundation \
    -framework CoreGraphics \
    -framework Foundation \
    -framework ImageIO \
    -framework Metal \
    -framework MetalKit \
    -framework QuartzCore \
    -framework UIKit \
    -framework UniformTypeIdentifiers \
    -o "$output"
}

link_shell iphonesimulator arm64-apple-ios15.0-simulator "$base/snake_ios_simulator.a" "$tmp/sgl-ios-snake-simulator"
link_shell iphoneos arm64-apple-ios15.0 "$base/snake_ios_device.a" "$tmp/sgl-ios-snake-device"

for executable in "$tmp/sgl-ios-snake-simulator" "$tmp/sgl-ios-snake-device"; do
  file "$executable" | grep -q 'Mach-O 64-bit executable arm64'
  /usr/bin/xcrun nm -gU "$executable" >"$executable.symbols"
  grep -q '_ScalaNativeInit' "$executable.symbols"
  grep -q '_sgl_ios_attach_metal_layer' "$executable.symbols"
  grep -q '_sgl_ios_frame' "$executable.symbols"
  grep -q '_sgl_metal_begin_frame' "$executable.symbols"
done

echo "linked Snake Swift/Metal shells for iOS Simulator and device"
