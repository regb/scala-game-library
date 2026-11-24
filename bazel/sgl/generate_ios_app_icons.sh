#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "usage: $0 <1024x1024 source.png> <output-directory>" >&2
  exit 2
fi

source_icon="$1"
output_dir="$2"

width="$(/usr/bin/sips -g pixelWidth "$source_icon" | /usr/bin/awk '/pixelWidth:/ { print $2 }')"
height="$(/usr/bin/sips -g pixelHeight "$source_icon" | /usr/bin/awk '/pixelHeight:/ { print $2 }')"
has_alpha="$(/usr/bin/sips -g hasAlpha "$source_icon" | /usr/bin/awk '/hasAlpha:/ { print $2 }')"
if [[ "$width" != 1024 || "$height" != 1024 ]]; then
  echo "iOS app icon source must be 1024x1024; got ${width}x${height}: $source_icon" >&2
  exit 1
fi
if [[ "$has_alpha" != no ]]; then
  echo "iOS app icon source must not contain transparency: $source_icon" >&2
  exit 1
fi

mkdir -p "$output_dir"
while read -r filename size; do
  /usr/bin/sips --resampleHeightWidth "$size" "$size" "$source_icon" --out "$output_dir/$filename" >/dev/null
done <<'SIZES'
AppIcon-20.png 20
AppIcon-20@2x.png 40
AppIcon-20@3x.png 60
AppIcon-29.png 29
AppIcon-29@2x.png 58
AppIcon-29@3x.png 87
AppIcon-40.png 40
AppIcon-40@2x.png 80
AppIcon-40@3x.png 120
AppIcon-60@2x.png 120
AppIcon-60@3x.png 180
AppIcon-76.png 76
AppIcon-76@2x.png 152
AppIcon-83.5@2x.png 167
AppIcon-1024.png 1024
SIZES
