"""Assembles the bundled Chrome."""

def _chrome_bundle_impl(ctx):
    output = ctx.actions.declare_directory(ctx.attr.name)
    args = ctx.actions.args()
    args.use_param_file("@%s")
    args.set_param_file_format("multiline")
    args.add(output.path)
    for kind in ["chrome", "library", "font", "license"]:
        args.add_all(getattr(ctx.files, kind), format_each = kind + "=%s")
    ctx.actions.run(
        inputs = ctx.files.chrome + ctx.files.library + ctx.files.font + ctx.files.license,
        outputs = [output],
        executable = ctx.executable._tool,
        arguments = [args],
        mnemonic = "ChromeBundle",
        # Large, and quicker to copy together than to fetch.
        execution_requirements = {"no-cache": "1"},
    )
    return [DefaultInfo(files = depset([output]), runfiles = ctx.runfiles([output]))]

chrome_bundle = rule(
    implementation = _chrome_bundle_impl,
    attrs = {
        "chrome": attr.label_list(allow_files = True),
        "font": attr.label_list(allow_files = True),
        "library": attr.label_list(allow_files = True),
        "license": attr.label_list(allow_files = True),
        "_tool": attr.label(default = ":bundle_tool", executable = True, cfg = "exec"),
    },
    doc = "A directory with Chrome and everything it loads.",
)
