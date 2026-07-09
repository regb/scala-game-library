load("@rules_java//java/common:java_info.bzl", "JavaInfo")


def _scala_native_transition_impl(settings, attr):
    return {"//command_line_option:platforms": ["//bazel/platforms:scala_native_x86"]}

_scala_native_transition = transition(
    implementation = _scala_native_transition_impl,
    inputs = [],
    outputs = ["//command_line_option:platforms"],
)


def _scala_native_binary_impl(ctx):
    transitive_jars = []
    for dep in ctx.attr.deps:
        if JavaInfo in dep:
            transitive_jars.extend(dep[JavaInfo].transitive_runtime_jars.to_list())
        transitive_jars.extend(dep.files.to_list())

    seen = {}
    unique_jars = []
    for jar in transitive_jars:
        if jar.path not in seen:
            seen[jar.path] = True
            unique_jars.append(jar)

    native_output = ctx.actions.declare_file(ctx.attr.name + ".bin")
    wrapper = ctx.actions.declare_file(ctx.attr.name)
    data_files = ctx.files.data
    workdir = ctx.actions.declare_directory(ctx.attr.name + "_native_work")
    classpath = ":".join([jar.path for jar in unique_jars])

    args = ctx.actions.args()
    args.add(classpath)
    args.add(ctx.attr.main_class)
    args.add(workdir.path)
    args.add(native_output.path)
    args.add_all(ctx.attr.linkopts)

    ctx.actions.run(
        inputs = unique_jars,
        tools = [ctx.executable.linker],
        outputs = [native_output, workdir],
        executable = ctx.executable.linker,
        arguments = [args],
        mnemonic = "ScalaNativeLink",
        progress_message = "Linking Scala Native binary %s" % ctx.label,
    )

    script = """#!/usr/bin/env bash
set -euo pipefail
self="$0"
runfiles_dir="${RUNFILES_DIR:-${self}.runfiles}"
workspace="_main"
if [[ ! -d "${runfiles_dir}/${workspace}" ]]; then
  workspace="%s"
fi
if [[ ! -d "${runfiles_dir}/${workspace}" ]]; then
  echo "Could not find Bazel runfiles workspace under ${runfiles_dir}" >&2
  exit 1
fi
cd "${runfiles_dir}/${workspace}"
exec "${runfiles_dir}/${workspace}/%s" "$@"
""" % (ctx.workspace_name, native_output.short_path)
    ctx.actions.write(output = wrapper, content = script, is_executable = True)

    runfiles = ctx.runfiles(files = [native_output] + data_files)
    return [DefaultInfo(files = depset([wrapper, native_output]), executable = wrapper, runfiles = runfiles)]

scala_native_binary = rule(
    implementation = _scala_native_binary_impl,
    attrs = {
        "deps": attr.label_list(providers = [JavaInfo], cfg = _scala_native_transition),
        "main_class": attr.string(mandatory = True),
        "linkopts": attr.string_list(default = ["-lSDL2", "-lSDL2_image", "-lGL", "-lGLESv2"]),
        "data": attr.label_list(allow_files = True),
        "linker": attr.label(default = Label("//bazel/scalanative:linker"), executable = True, cfg = "exec"),
    },
    executable = True,
)
