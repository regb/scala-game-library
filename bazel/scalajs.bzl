#load(":cross.bzl", "scala_library")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load("@rules_scala//scala:scala.bzl", _scala_library_rule = "scala_library")

def _scalajs_transition_impl(settings, attr):
    return {"//command_line_option:platforms": ["//bazel/platforms:scala_js"]}

_scalajs_transition = transition(
    implementation = _scalajs_transition_impl,
    inputs = [],
    outputs = ["//command_line_option:platforms"]
)

def _scalajs_library_impl(ctx):
    """Implementation that forwards to the underlying scala_library with ScalaJS platform."""
    
    underlying_lib = ctx.attr.underlying_lib
    
    providers = []
    if DefaultInfo in underlying_lib:
        providers.append(underlying_lib[DefaultInfo])
    if JavaInfo in underlying_lib:
        providers.append(underlying_lib[JavaInfo])
    
    return providers

_scalajs_library_rule = rule(
    implementation = _scalajs_library_impl,
    attrs = {
        "underlying_lib": attr.label(
            mandatory = True,
            providers = [JavaInfo],
        ),
    },
    cfg = _scalajs_transition,
    provides = [DefaultInfo, JavaInfo],
    doc = "ScalaJS library with incoming transition",
)

def scalajs_library(name, deps=[], visibility=None, **kwargs):
    """A Scala library that automatically forces ScalaJS platform for itself and dependencies."""
    
    underlying_lib_name = name + "_impl"
    scala_library_attrs = dict(kwargs)
    _scala_library_rule(
        name = underlying_lib_name,
        deps = deps+[Label("@maven//:org_scala_js_scalajs_library_2_13")],
        plugins = [Label("@maven//:org_scala_js_scalajs_compiler_2_13_18")],
        #target_compatible_with = [Label("//bazel/platforms:scala_js")],
        target_compatible_with = [Label("//bazel/platforms:compiler_js")],
        visibility = ["//visibility:private"],
        **scala_library_attrs
    )
    
    # Create the wrapper rule with incoming transition
    # This will transition the underlying_lib AND all its dependencies to ScalaJS platform
    wrapper_attrs = {}
    if visibility:
        wrapper_attrs["visibility"] = visibility
    
    for attr in ["testonly", "tags"]:
        if attr in kwargs:
            wrapper_attrs[attr] = kwargs[attr]
    
    _scalajs_library_rule(
        name = name,
        underlying_lib = ":" + underlying_lib_name,
        **wrapper_attrs
    )

def scalajs_module(name, srcs, deps, output_name, **params):
    libname = name + "_lib"
    scalajs_library(
        name = libname,
        srcs = srcs,
        deps = deps,
    )

    scalajs_link(
        name = name,
        scala_libs = [":" + libname],
        linker = Label("//bazel/scalajs:linker"),
        output_name = output_name,
        **params,
    )

def _scalajs_link_impl(ctx):
    """Implementation of the scalajs_link rule."""
    
    transitive_jars = []
    for scala_lib in ctx.attr.scala_libs:
        if hasattr(scala_lib, "files"):
            transitive_jars.extend(scala_lib.files.to_list())
        
        if JavaInfo in scala_lib:
            java_info = scala_lib[JavaInfo]
            transitive_jars.extend(java_info.transitive_runtime_jars.to_list())
    
    seen = {}
    unique_jars = []
    for jar in transitive_jars:
        if jar.path not in seen:
            seen[jar.path] = True
            unique_jars.append(jar)
    
    classpath_parts = [jar.path for jar in unique_jars]
    classpath = ":".join(classpath_parts)
    
    output = ctx.actions.declare_file(ctx.attr.output_name)
    
    args = ctx.actions.args()
    args.add(classpath)
    
    if ctx.attr.main_class:
        main_method = ctx.attr.main_method if ctx.attr.main_method else "main"
        args.add(ctx.attr.main_class)
        args.add(main_method)
    
    args.add(output.path)
    
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
            cfg = _scalajs_transition,
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
