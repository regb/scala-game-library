load("@rules_go//go:def.bzl", "go_binary")
load(":bundle.bzl", "chrome_bundle")

# Chrome, its libraries and a font: it uses nothing of the system but the kernel.
chrome_bundle(
    name = "linux64",
    chrome = ["@sgl_chrome_linux64//:chrome"],
    font = ["@sgl_chrome_linux64//:fonts"],
    library = ["@sgl_chrome_linux64//:libraries"],
    license = ["@sgl_chrome_linux64//:licenses"],
    target_compatible_with = [
        "@platforms//cpu:x86_64",
        "@platforms//os:linux",
    ],
    visibility = ["//backends/html5/browser-testing:__pkg__"],
)

go_binary(
    name = "bundle_tool",
    srcs = ["bundle.go"],
)
