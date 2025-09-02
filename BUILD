load("@rules_scala//scala:scala.bzl", "setup_scala_toolchain")
load(
    "@rules_scala//scala:scala.bzl",
    "scala_library", "scala_binary",
)

#setup_scala_toolchain(
#    name = "my_toolchain",
#    # configure toolchain dependencies
#    parser_combinators_deps = [
#        "@maven//:org_scala_lang_modules_scala_parser_combinators_2_12",
#    ],
#    scala_compile_classpath = [
#        "@maven//:org_scala_lang_scala_compiler",
#        "@maven//:org_scala_lang_scala_library",
#        "@maven//:org_scala_lang_scala_reflect",
#    ],
#    scala_library_classpath = [
#        "@maven//:org_scala_lang_scala_library",
#        "@maven//:org_scala_lang_scala_reflect",
#    ],
#    scala_macro_classpath = [
#        "@maven//:org_scala_lang_scala_library",
#        "@maven//:org_scala_lang_scala_reflect",
#    ],
#    scala_xml_deps = [
#        "@maven//:org_scala_lang_modules_scala_xml_2_12",
#    ],
#    # example of setting attribute values
#    scalacopts = ["-Ywarn-unused"],
#    unused_dependency_checker_mode = "off",
#    visibility = ["//visibility:public"]
#)

scala_library(
    name = "Test.js",
    srcs = [ "Test.scala" ],
    plugins = [ "@maven//:org_scala_js_scalajs_compiler_2_13_16"],
    deps = [ "@maven//:org_scala_js_scalajs_library_2_13" ],
)

scala_binary(
    name = "linker",
    srcs = ["ScalaJsLinker.scala"],
    deps = [
        "@maven//:org_scala_js_scalajs_ir_2_13",
        "@maven//:org_scala_js_scalajs_linker_2_13",
        "@maven//:org_scala_js_scalajs_linker_interface_2_13",
        "@maven//:org_scala_js_scalajs_logging_2_13",
    ],
    main_class = "ScalaJsLinker",
    visibility = ["//visibility:public"]
)

genrule(
    name = "index",
    tools = [":linker"],
    srcs = [
        ":Test.js",
        "@maven//:org_scala_js_scalajs_library_2_13",
        "@maven//:org_scala_js_scalajs_javalib",
        "@maven//:org_scala_js_scalajs_scalalib_2_13",
    ],
    cmd = "./$(location :linker) \"$(location :Test.js):$(location @maven//:org_scala_js_scalajs_library_2_13):$(location @maven//:org_scala_js_scalajs_javalib):$(location @maven//:org_scala_js_scalajs_scalalib_2_13)\" Test main $@",
    outs = ["index.js"],
)

