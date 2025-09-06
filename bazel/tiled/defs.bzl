def _tiled_export_impl(ctx):
    """Implementation function for the tiled_export rule."""
    output_files = []
    
    tiled_executable = ctx.executable._tiled_tool
    
    for src_file in ctx.files.srcs:
        output_filename = ctx.attr.output_prefix + "/" + src_file.basename
        output_file = ctx.actions.declare_file(output_filename)
        output_files.append(output_file)
        
        ctx.actions.run_shell(
            inputs = [src_file] + ctx.files.tilesets,
            outputs = [output_file],
            tools = [tiled_executable],
            # TODO: we need to handled the tiled/levels prefix in a general way that it works with any layouts.
            command = "{} --embed-tilesets --detach-templates --resolve-types-and-properties --export-map {} {} && cp {} {}".format(
                tiled_executable.path, src_file.path, "tiled/levels/exported.json", "tiled/levels/exported.json", output_file.path
            ),
            mnemonic = "TiledExport",
            progress_message = "Exporting Tiled map %s to %s" % (src_file.short_path, output_file.short_path),
            execution_requirements = {
                "local": "1",
                "no-sandbox": "1",
            },
        )
    
    return [DefaultInfo(files = depset(output_files))]

tiled_export = rule(
    implementation = _tiled_export_impl,
    attrs = {
        "srcs": attr.label_list(
            allow_files = [".json", ".tmx"],
            mandatory = True,
            doc = "List of Tiled map files to process",
        ),
        "tilesets": attr.label_list(
            allow_files = [".json", ".tmx"],
            mandatory = True,
            doc = "List of Tiled tilesets",
        ),
        "assets": attr.label_list(
            mandatory = False,
            doc = "List of assets such as images for tilesets",
        ),
        "output_prefix": attr.string(
            mandatory = True,
            doc = "Folder prefix for output files (e.g., 'assets/levels')",
        ),
        "_tiled_tool": attr.label(
            executable = True,
            cfg = "exec",
            default = "@tiled_tool//:tiled",
            doc = "The Tiled executable to use (private attribute)",
        ),
    },
    doc = "Processes Tiled map files and exports them with embedded tilesets and resolved properties",
)
