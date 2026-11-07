#!/usr/bin/env bash
set -euo pipefail

workspace="${TEST_SRCDIR}/_main"
if [[ ! -d "$workspace" ]]; then
  workspace="${TEST_SRCDIR}/${TEST_WORKSPACE}"
fi
archive="$workspace/examples/ios-static-library/ios_simulator.a"
header="$workspace/examples/ios-static-library/sgl_ios.h"
source="$workspace/examples/ios-static-library/IOSSmoke.swift"
shell_source="$workspace/examples/ios-static-library/IOSMetalShell.swift"
out="${TEST_TMPDIR}/sgl-ios-smoke"
shell_out="${TEST_TMPDIR}/sgl-ios-metal-shell"
sdk="$(/usr/bin/xcrun --sdk iphonesimulator --show-sdk-path)"

/usr/bin/xcrun --sdk iphonesimulator swiftc \
  -target arm64-apple-ios15.0-simulator \
  -sdk "$sdk" \
  -import-objc-header "$header" \
  "$source" "$archive" \
  -o "$out"
/usr/bin/xcrun --sdk iphonesimulator swiftc \
  -parse-as-library \
  -target arm64-apple-ios15.0-simulator \
  -sdk "$sdk" \
  -import-objc-header "$header" \
  "$shell_source" "$archive" \
  -framework UIKit \
  -framework MetalKit \
  -o "$shell_out"

file "$out" >"${TEST_TMPDIR}/file.txt"
file "$shell_out" >"${TEST_TMPDIR}/shell-file.txt"
/usr/bin/xcrun nm -gU "$out" >"${TEST_TMPDIR}/symbols.txt"
grep -q 'Mach-O 64-bit executable arm64' "${TEST_TMPDIR}/file.txt"
grep -q 'Mach-O 64-bit executable arm64' "${TEST_TMPDIR}/shell-file.txt"
grep -q '_ScalaNativeInit' "${TEST_TMPDIR}/symbols.txt"
grep -q '_sgl_add' "${TEST_TMPDIR}/symbols.txt"
echo "linked Swift simulator executable with Scala Native static library"
