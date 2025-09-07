constraint_setting(name = "compiler_backend")

constraint_value(
    name = "compiler_jvm",
    constraint_setting = ":compiler_backend",
    visibility = ["//visibility:public"],
)
constraint_value(
    name = "compiler_js",
    constraint_setting = ":compiler_backend",
    visibility = ["//visibility:public"],
)
constraint_value(
    name = "compiler_native",
    constraint_setting = ":compiler_backend",
    visibility = ["//visibility:public"],
)

platform(
    name="scala_jvm",
    constraint_values = [
        ":compiler_jvm",
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
    visibility = ["//visibility:public"],
)
platform(
    name="scala_js",
    constraint_values = [
       ":compiler_js",
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
    visibility = ["//visibility:public"],
)
platform(
    name="scala_native_x86",
    constraint_values = [
        ":compiler_native",
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
    visibility = ["//visibility:public"],
)
