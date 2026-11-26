"""Scala 3 Scaladoc rule operating on compiled .tasty JARs."""

load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _scala3_doc_impl(ctx):
    out_dir = ctx.actions.declare_directory(ctx.attr.name)
    java_runtime = ctx.attr._jdk[java_common.JavaRuntimeInfo]
    tool_cp = ctx.attr._scaladoc[JavaInfo].transitive_runtime_jars

    input_jars = []
    classpath_depsets = []
    for dep in ctx.attr.deps:
        java_info = dep[JavaInfo]
        input_jars.extend(java_info.runtime_output_jars)
        classpath_depsets.append(java_info.transitive_runtime_jars)
        classpath_depsets.append(java_info.transitive_compile_time_jars)

    classpath = depset(transitive = classpath_depsets)

    # Scala 3 scaladoc expects unpacked .tasty directories rather than .jar paths.
    # Unpack input JARs with the hermetic JDK's jar tool, then run dotty.tools.scaladoc.Main.
    jar_exec = java_runtime.java_home + "/bin/jar"
    java_exec = java_runtime.java_executable_exec_path

    args = ctx.actions.args()
    args.add(java_exec)
    args.add(jar_exec)
    args.add(out_dir.path)
    args.add(ctx.attr.project_name)
    args.add(ctx.attr.project_version)
    args.add(ctx.attr.source_links)
    args.add(ctx.file.logo.path if ctx.file.logo else "")
    args.add_joined(tool_cp, join_with = ctx.configuration.host_path_separator)
    args.add_joined(classpath, join_with = ctx.configuration.host_path_separator)
    args.add(str(len(ctx.attr.social_links)))
    args.add_all(ctx.attr.social_links)
    args.add(str(len(ctx.attr.extra_args)))
    args.add_all(ctx.attr.extra_args)
    args.add_all(input_jars)

    extra_inputs = [ctx.file.logo] if ctx.file.logo else []

    script = """set -euo pipefail
JAVA="$1"; shift
JAR="$1"; shift
OUT_DIR="$1"; shift
PROJECT="$1"; shift
VERSION="$1"; shift
SOURCE_LINKS="$1"; shift
LOGO="$1"; shift
TOOL_CP="$1"; shift
CLASSPATH="$1"; shift

NUM_SOCIAL="$1"; shift
SOCIAL_ARGS=()
for ((i = 0; i < NUM_SOCIAL; i++)); do
  SOCIAL_ARGS+=("-social-links:$1")
  shift
done

NUM_EXTRA="$1"; shift
EXTRA_ARGS=()
for ((i = 0; i < NUM_EXTRA; i++)); do
  EXTRA_ARGS+=("$1")
  shift
done

ORIG_PWD="$PWD"
TASTY_DIR="$ORIG_PWD/${OUT_DIR}.tasty_tmp"
rm -rf "$TASTY_DIR"
mkdir -p "$TASTY_DIR"
trap 'rm -rf "$TASTY_DIR"' EXIT

for jar_file in "$@"; do
  (cd "$TASTY_DIR" && "$ORIG_PWD/$JAR" xf "$ORIG_PWD/$jar_file")
done

CMD=(
  "$JAVA"
  -cp "$TOOL_CP"
  dotty.tools.scaladoc.Main
  -d "$OUT_DIR"
  -project "$PROJECT"
)
if [[ -n "$VERSION" ]]; then
  CMD+=(-project-version "$VERSION")
fi
if [[ -n "$SOURCE_LINKS" ]]; then
  CMD+=("-source-links:$SOURCE_LINKS")
fi
if [[ -n "$LOGO" ]]; then
  CMD+=(-project-logo "$LOGO")
fi
if [[ ${#SOCIAL_ARGS[@]} -gt 0 ]]; then
  CMD+=("${SOCIAL_ARGS[@]}")
fi
if [[ ${#EXTRA_ARGS[@]} -gt 0 ]]; then
  CMD+=("${EXTRA_ARGS[@]}")
fi
CMD+=(-classpath "$CLASSPATH" "$TASTY_DIR")

"${CMD[@]}"
"""

    ctx.actions.run_shell(
        inputs = depset(
            direct = input_jars + extra_inputs,
            transitive = [classpath, tool_cp, java_runtime.files],
        ),
        outputs = [out_dir],
        command = script,
        arguments = [args],
        use_default_shell_env = True,
        mnemonic = "Scala3Doc",
        progress_message = "Generating Scala 3 Scaladoc for %s" % ctx.label,
    )

    return [DefaultInfo(files = depset([out_dir]))]

scala3_doc = rule(
    implementation = _scala3_doc_impl,
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
            doc = "Scala 3 library targets whose compiled JARs (.tasty) are documented.",
        ),
        "project_name": attr.string(
            default = "SGL",
            doc = "Project title displayed in the generated Scaladoc.",
        ),
        "project_version": attr.string(
            default = "",
            doc = "Optional project version string.",
        ),
        "source_links": attr.string(
            default = "",
            doc = "Scaladoc -source-links specification (e.g. github://owner/repo/branch).",
        ),
        "social_links": attr.string_list(
            default = [],
            doc = "Scaladoc -social-links entries (e.g. github::https://...).",
        ),
        "logo": attr.label(
            allow_single_file = True,
            doc = "Optional project logo file passed via -project-logo.",
        ),
        "extra_args": attr.string_list(
            default = [],
            doc = "Additional flags forwarded to dotty.tools.scaladoc.Main.",
        ),
        "_jdk": attr.label(
            cfg = "exec",
            default = Label("@rules_java//toolchains:remotejdk_17"),
            providers = [java_common.JavaRuntimeInfo],
        ),
        "_scaladoc": attr.label(
            cfg = "exec",
            default = Label("@sgl_docs_maven//:org_scala_lang_scaladoc_3"),
            providers = [JavaInfo],
        ),
    },
    doc = "Generates Scala 3 Scaladoc HTML documentation from compiled .tasty JARs.",
)
