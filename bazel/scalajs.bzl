def _scalajs_link_impl(ctx):
    """Implementation of the scalajs_link rule."""
    
    # Get all scala library targets
    scala_libs = ctx.attr.scala_libs
    
    # Collect all transitive dependencies from all libraries
    transitive_jars = []
    
    for scala_lib in scala_libs:
        # Add the library jar
        if hasattr(scala_lib, "files"):
            transitive_jars.extend(scala_lib.files.to_list())
        
        # Collect transitive runtime dependencies
        if JavaInfo in scala_lib:
            java_info = scala_lib[JavaInfo]
            transitive_jars.extend(java_info.transitive_runtime_jars.to_list())
    
    # Remove duplicates while preserving order
    seen = {}
    unique_jars = []
    for jar in transitive_jars:
        if jar.path not in seen:
            seen[jar.path] = True
            unique_jars.append(jar)
    
    # Build the classpath string for the linker
    classpath_parts = [jar.path for jar in unique_jars]
    classpath = ":".join(classpath_parts)
    
    # Create the output file
    output = ctx.actions.declare_file(ctx.attr.output_name)
    
    # Build the command
    args = ctx.actions.args()
    args.add(classpath)
    
    # Add main_class and main_method if main_class is provided
    if ctx.attr.main_class:
        main_method = ctx.attr.main_method if ctx.attr.main_method else "main"
        args.add(ctx.attr.main_class)
        args.add(main_method)
    
    args.add(output.path)
    
    # Run the linker
    ctx.actions.run(
        inputs = unique_jars,
        outputs = [output],
        executable = ctx.executable.linker,
        arguments = [args],
        mnemonic = "ScalaJsLink",
        progress_message = "Linking Scala.js application %s" % ctx.label,
    )
    
    return [DefaultInfo(files = depset([output]))]

scalajs_link = rule(
    implementation = _scalajs_link_impl,
    attrs = {
        "scala_libs": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
            doc = "The scala library targets to link",
        ),
        "linker": attr.label(
            mandatory = True,
            executable = True,
            cfg = "exec",
            doc = "The ScalaJS linker binary",
        ),
        "main_class": attr.string(
            mandatory = False,
            doc = "The main class name (optional - If provided, the scala library must provide an entry point)",
        ),
        "main_method": attr.string(
            mandatory = False,
            doc = "The main method name (optional - only active if main_class is provided, defaults to 'main')",
        ),
        "output_name": attr.string(
            mandatory = True,
            doc = "The name of the output JavaScript file",
        ),
    },
    doc = "Links Scala.js libraries into a JavaScript file",
)
