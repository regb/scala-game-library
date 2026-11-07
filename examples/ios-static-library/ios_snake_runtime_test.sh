#!/usr/bin/env bash
set -euo pipefail

workspace="${TEST_SRCDIR}/_main"
if [[ ! -d "$workspace" ]]; then
  workspace="${TEST_SRCDIR}/${TEST_WORKSPACE}"
fi
base="$workspace/examples/ios-static-library"
bridge="$workspace/backends/metal-native/metal_bridge.m"
archive="$base/snake_ios_simulator.a"
header="$base/sgl_ios.h"
source="$base/IOSMetalShell.swift"
plist="$base/Info.plist"
validator="$base/validate_snake_frame.py"
sdk="$(/usr/bin/xcrun --sdk iphonesimulator --show-sdk-path)"
app="${TEST_TMPDIR}/SGLIOSSnake.app"
bundle_id="org.scala-game-library.snake"
device_name="SGL Snake Runtime ${RANDOM}-$$"
device=""

cleanup() {
  if [[ -n "$device" ]]; then
    /usr/bin/xcrun simctl terminate "$device" "$bundle_id" >/dev/null 2>&1 || true
    /usr/bin/xcrun simctl shutdown "$device" >/dev/null 2>&1 || true
    /usr/bin/xcrun simctl delete "$device" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

selection="$(/usr/bin/xcrun simctl list runtimes -j | /usr/bin/python3 -c '
import json, sys
runtimes = [r for r in json.load(sys.stdin)["runtimes"] if r.get("isAvailable", True) and r.get("platform") == "iOS"]
if not runtimes:
    raise SystemExit("no available iOS Simulator runtime")
runtime = max(runtimes, key=lambda r: len(r.get("supportedDeviceTypes", [])))
devices = [d for d in runtime.get("supportedDeviceTypes", []) if d.get("productFamily") == "iPhone"]
if not devices:
    raise SystemExit("the selected iOS runtime supports no iPhone device type")
preferred = next((d for d in devices if d["identifier"].endswith("iPhone-16-Pro")), None)
device = preferred or devices[0]
print(runtime["identifier"] + " " + device["identifier"])
')"
runtime="${selection%% *}"
device_type="${selection#* }"

mkdir -p "$app"
/usr/bin/xcrun --sdk iphonesimulator clang \
  -target arm64-apple-ios15.0-simulator \
  -isysroot "$sdk" \
  -fobjc-arc \
  -fmodules \
  -c "$bridge" \
  -o "${TEST_TMPDIR}/metal_bridge.o"
/usr/bin/xcrun --sdk iphonesimulator swiftc \
  -parse-as-library \
  -target arm64-apple-ios15.0-simulator \
  -sdk "$sdk" \
  -import-objc-header "$header" \
  "$source" "$archive" "${TEST_TMPDIR}/metal_bridge.o" \
  -framework AVFoundation \
  -framework CoreGraphics \
  -framework Foundation \
  -framework ImageIO \
  -framework Metal \
  -framework MetalKit \
  -framework QuartzCore \
  -framework UIKit \
  -framework UniformTypeIdentifiers \
  -o "$app/SGLIOSSnake"
cp "$plist" "$app/Info.plist"
/usr/bin/codesign --force --sign - --timestamp=none "$app"

device="$(/usr/bin/xcrun simctl create "$device_name" "$device_type" "$runtime")"
/usr/bin/xcrun simctl boot "$device"
/usr/bin/xcrun simctl bootstatus "$device" -b
/usr/bin/xcrun simctl install "$device" "$app"
launch_output="$(/usr/bin/xcrun simctl launch "$device" "$bundle_id")"
pid="${launch_output##*: }"

frame="${TEST_TMPDIR}/snake.png"
for _ in $(seq 1 20); do
  sleep 1
  /usr/bin/xcrun simctl io "$device" screenshot "$frame" >/dev/null
  if /usr/bin/python3 "$validator" colors "$frame" >/dev/null 2>&1; then
    break
  fi
done
/usr/bin/python3 "$validator" colors "$frame"

/usr/bin/xcrun simctl launch "$device" com.apple.Preferences >/dev/null
sleep 1
resume_output="$(/usr/bin/xcrun simctl launch "$device" "$bundle_id")"
resume_pid="${resume_output##*: }"
if [[ "$resume_pid" != "$pid" ]]; then
  echo "Snake process restarted across background/resume: $pid -> $resume_pid" >&2
  exit 1
fi

/usr/bin/lldb -p "$pid" \
  -o 'expression (void)sgl_ios_pointer(0, 1.0f, 10000.0f, 0)' \
  -o detach \
  -o quit \
  --batch >"${TEST_TMPDIR}/pointer-lldb.log" 2>&1
sleep 1
touch_frame="${TEST_TMPDIR}/snake-touch.png"
/usr/bin/xcrun simctl io "$device" screenshot "$touch_frame" >/dev/null
/usr/bin/python3 "$validator" vertical "$touch_frame"

if [[ -n "${TEST_UNDECLARED_OUTPUTS_DIR:-}" ]]; then
  cp "$frame" "$TEST_UNDECLARED_OUTPUTS_DIR/snake.png"
  cp "$touch_frame" "$TEST_UNDECLARED_OUTPUTS_DIR/snake-touch.png"
  cp "${TEST_TMPDIR}/pointer-lldb.log" "$TEST_UNDECLARED_OUTPUTS_DIR/pointer-lldb.log"
fi

echo "launched, resumed, pixel-validated, and redirected SGL Snake in iOS Simulator"
