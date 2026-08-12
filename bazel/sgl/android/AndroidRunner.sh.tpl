#!/usr/bin/env bash
set -euo pipefail

workspace="${BUILD_WORKSPACE_DIRECTORY:-}"
if [[ -z "$workspace" ]]; then
  workspace="$(git rev-parse --show-toplevel)"
fi

project_dir="$workspace/@PROJECT_DIR@"
sgl_android_root="@SGL_ANDROID_REPO_ROOT@"
if [[ "$sgl_android_root" != /* ]]; then
  execution_root="$(cd "$workspace" && bazel info execution_root)"
  sgl_android_root="$(cd "$execution_root/$sgl_android_root" && pwd)"
fi
script_dir="$(cd "$(dirname "$0")" && pwd)"
template_dir="$script_dir/@PROJECT_TEMPLATE_DIR@"

cd "$workspace"
bazel build @GAME_LABELS@
cd "$sgl_android_root"
bazel build //core:sgl-core //modules:sgl-scene2d //modules:sgl-particles

rm -rf "$project_dir"
mkdir -p "$project_dir"
cp -R "$template_dir/." "$project_dir/"
chmod -R u+w "$project_dir"

assets_dir="@ASSETS_DIR@"
if [[ -n "$assets_dir" && -d "$workspace/$assets_dir" ]]; then
  mkdir -p "$project_dir/app/src/main/res" "$project_dir/app/src/main/assets"
  find "$workspace/$assets_dir" -maxdepth 1 -type d -name 'drawable*' -exec cp -R '{}' "$project_dir/app/src/main/res/" \;
  for asset_subdir in audio fonts levels; do
    if [[ -d "$workspace/$assets_dir/$asset_subdir" ]]; then
      cp -R "$workspace/$assets_dir/$asset_subdir" "$project_dir/app/src/main/assets/"
    fi
  done
fi

android_resources_dir="@ANDROID_RESOURCES_DIR@"
if [[ -n "$android_resources_dir" && -d "$workspace/$android_resources_dir" ]]; then
  mkdir -p "$project_dir/app/src/main/res"
  for resource_path in "$workspace/$android_resources_dir"/*; do
    [[ -e "$resource_path" ]] || continue
    resource_name="$(basename "$resource_path")"
    if [[ -d "$resource_path" ]]; then
      mkdir -p "$project_dir/app/src/main/res/$resource_name"
      cp -R "$resource_path/." "$project_dir/app/src/main/res/$resource_name/"
    else
      cp "$resource_path" "$project_dir/app/src/main/res/"
    fi
  done
fi

google_services_json="@GOOGLE_SERVICES_JSON@"
if [[ -n "$google_services_json" && -f "$workspace/$google_services_json" ]]; then
  cp "$workspace/$google_services_json" "$project_dir/app/google-services.json"
fi

python3 - "$project_dir" "$workspace" "$sgl_android_root" <<'PY'
from pathlib import Path
import sys
project_dir = Path(sys.argv[1])
workspace = sys.argv[2]
sgl_android_root = sys.argv[3]
for path in project_dir.rglob("*"):
    if path.is_file():
        try:
            text = path.read_text()
        except UnicodeDecodeError:
            continue
        text = text.replace("@WORKSPACE@", workspace)
        text = text.replace("@SGL_ANDROID_ROOT@", sgl_android_root)
        path.write_text(text)
PY

cd "$project_dir"
@GRADLE_ENV@
"$sgl_android_root/android-kotlin/gradlew" @GRADLE_TASK@ -PsglBazelRepo="$sgl_android_root"
@LAUNCH_CMD@
