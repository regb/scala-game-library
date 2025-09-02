constraint_setting(name = "compiler_backend")

constraint_value(
    name = "compiler_jvm",
    constraint_setting = ":compiler_backend"
)
constraint_value(
    name = "compiler_js",
    constraint_setting = ":compiler_backend"
)
constraint_value(
    name = "compiler_native",
    constraint_setting = ":compiler_backend"
)

platform(
    name="scala_jvm",
    constraint_values = ["//bazel/constraints:compiler_jvm"]
)
platform(
    name="scala_js",
    constraint_values = ["//bazel/constraints:compiler_js"]
)
platform(
    name="scala_native_x86",
    constraint_values = [
        "//bazel/constraints:compiler_native",
        "@platforms//cpu:x86_64",
    ]
)
