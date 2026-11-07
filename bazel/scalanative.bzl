load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _scala_native_transition_impl(settings, attr):
    return {"//command_line_option:platforms": [str(attr.target_platform)]}

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

    native_objects = []
    for source in ctx.files.native_srcs:
        object_file = ctx.actions.declare_file(ctx.attr.name + "_native/" + source.basename + ".o")
        ctx.actions.run(
            inputs = [source],
            outputs = [object_file],
            executable = "/usr/bin/clang",
            arguments = ctx.attr.native_copts + [
                "-c",
                source.path,
                "-o",
                object_file.path,
            ],
            mnemonic = "NativeCompile",
            progress_message = "Compiling native bridge %s" % source.short_path,
        )
        native_objects.append(object_file)

    args = ctx.actions.args()
    args.add(classpath)
    args.add(ctx.attr.main_class)
    args.add(workdir.path)
    args.add(native_output.path)
    args.add_all(native_objects)
    args.add_all(ctx.attr.linkopts)

    ctx.actions.run(
        inputs = unique_jars + native_objects,
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

def _scala_native_static_library_impl(ctx):
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

    output = ctx.actions.declare_file(ctx.attr.name + ".a")
    workdir = ctx.actions.declare_directory(ctx.attr.name + "_native_work")
    classpath = ":".join([jar.path for jar in unique_jars])
    args = ctx.actions.args()
    args.add(classpath)
    args.add(workdir.path)
    args.add(output.path)
    args.add(ctx.attr.target_triple)
    args.add(ctx.executable.clang.path)
    args.add(ctx.executable.clang_pp.path)
    args.add_all(ctx.attr.linkopts)

    ctx.actions.run(
        inputs = unique_jars,
        tools = [ctx.executable.linker, ctx.executable.clang, ctx.executable.clang_pp],
        outputs = [output, workdir],
        executable = ctx.executable.linker,
        arguments = [args],
        env = {"PATH": "/usr/bin:/bin"},
        mnemonic = "ScalaNativeLibraryLink",
        progress_message = "Linking Scala Native static library %s" % ctx.label,
    )
    return [DefaultInfo(files = depset([output]))]

scala_native_static_library = rule(
    implementation = _scala_native_static_library_impl,
    attrs = {
        "deps": attr.label_list(providers = [JavaInfo], cfg = _scala_native_transition),
        "target_platform": attr.label(default = Label("//bazel/platforms:scala_native_macos_arm64")),
        "target_triple": attr.string(mandatory = True),
        "clang": attr.label(mandatory = True, executable = True, cfg = "exec"),
        "clang_pp": attr.label(mandatory = True, executable = True, cfg = "exec"),
        "linkopts": attr.string_list(),
        "linker": attr.label(default = Label("//bazel/scalanative:library_linker"), executable = True, cfg = "exec"),
    },
)

scala_native_binary = rule(
    implementation = _scala_native_binary_impl,
    attrs = {
        "deps": attr.label_list(providers = [JavaInfo], cfg = _scala_native_transition),
        "main_class": attr.string(mandatory = True),
        "linkopts": attr.string_list(default = ["-lSDL2", "-lSDL2_image", "-lGL", "-lGLESv2"]),
        "data": attr.label_list(allow_files = True),
        "native_srcs": attr.label_list(allow_files = [".c", ".m", ".mm"]),
        "native_copts": attr.string_list(),
        "target_platform": attr.label(default = Label("//bazel/platforms:scala_native_x86")),
        "linker": attr.label(default = Label("//bazel/scalanative:linker"), executable = True, cfg = "exec"),
    },
    executable = True,
)
