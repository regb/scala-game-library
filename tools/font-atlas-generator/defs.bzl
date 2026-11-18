"""Runs an executable target as a test."""

def _binary_test_impl(ctx):
    executable = ctx.actions.declare_file(ctx.label.name)
    ctx.actions.symlink(
        output = executable,
        target_file = ctx.executable.binary,
        is_executable = True,
    )
    runfiles = ctx.runfiles(files = ctx.files.data)
    runfiles = runfiles.merge(ctx.attr.binary[DefaultInfo].default_runfiles)
    return [DefaultInfo(executable = executable, runfiles = runfiles)]

binary_test = rule(
    implementation = _binary_test_impl,
    doc = "A test that passes when `binary`, run with the test's `args`, exits with 0.",
    attrs = {
        "binary": attr.label(
            doc = "The executable to run.",
            mandatory = True,
            executable = True,
            cfg = "target",
        ),
        "data": attr.label_list(
            doc = "Files the binary reads, available to `args` through $(rootpath).",
            allow_files = True,
        ),
    },
    test = True,
)
