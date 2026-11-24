#!/usr/bin/env bash
set -euo pipefail
sdk="$(/usr/bin/xcrun --sdk iphonesimulator --show-sdk-path)"
args=()
for arg in "$@"; do
  [[ "$arg" == "--emit-static-lib" ]] || args+=("$arg")
done
exec /usr/bin/xcrun --sdk iphonesimulator clang -target arm64-apple-ios15.0-simulator -isysroot "$sdk" "${args[@]}"
