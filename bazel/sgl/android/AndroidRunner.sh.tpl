#!/usr/bin/env bash
set -euo pipefail

workspace="${BUILD_WORKSPACE_DIRECTORY:-}"
if [[ -z "$workspace" ]]; then
  workspace="$(git rev-parse --show-toplevel)"
fi

project_dir="$workspace/@PROJECT_DIR@"
script_dir="$(cd "$(dirname "$0")" && pwd)"
template_dir="$script_dir/@PROJECT_TEMPLATE_DIR@"

cd "$workspace"
bazel build @CORE_LABEL@ //core:sgl-core //jvm-shared:jvm-shared //modules:sgl-scene2d //modules:sgl-particles

rm -rf "$project_dir"
mkdir -p "$project_dir"
cp -R "$template_dir/." "$project_dir/"
chmod -R u+w "$project_dir"

python3 - "$project_dir" "$workspace" <<'PY'
from pathlib import Path
import sys
project_dir = Path(sys.argv[1])
workspace = sys.argv[2]
for path in project_dir.rglob("*"):
    if path.is_file():
        text = path.read_text()
        text = text.replace("@WORKSPACE@", workspace)
        path.write_text(text)
PY

cd "$project_dir"
"$workspace/android-kotlin/gradlew" @GRADLE_TASK@ -PsglBazelRepo="$workspace"
@LAUNCH_CMD@
