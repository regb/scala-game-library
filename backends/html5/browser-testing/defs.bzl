"""Tests that run HTML5 games in a real browser."""

load("@rules_scala//scala:scala.bzl", "scala_test")

def html5_browser_test(name, srcs, sites, deps = [], data = [], **kwargs):
    """A ScalaTest suite that opens HTML5 games in a headless Chrome.

    On x86-64 Linux the browser comes with the test. Elsewhere it is one that is
    installed on the machine.

    Args:
        name: The name of the test.
        srcs: Suites, which extend sgl.html5.testing.BrowserSuite.
        sites: The `<name>_site` targets the suites open, by their path in the workspace.
        deps: Additional dependencies of the suites.
        data: Additional files of the suites.
        **kwargs: Passed to scala_test.
    """
    scala_test(
        name = name,
        srcs = srcs,
        data = data + sites,
        deps = deps + [Label("//backends/html5/browser-testing")],
        **kwargs
    )
