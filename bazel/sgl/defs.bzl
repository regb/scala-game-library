load(":core.bzl", _sgl_core_library = "sgl_core_library")
load(":desktop.bzl", _sgl_desktop_awt_app = "sgl_desktop_awt_app")
load(":html5.bzl", _sgl_html5_app = "sgl_html5_app")

sgl_core_library = _sgl_core_library
sgl_desktop_awt_app = _sgl_desktop_awt_app
sgl_html5_app = _sgl_html5_app
