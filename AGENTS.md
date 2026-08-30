SGL is a cross-platform Scala game library targeting JVM desktop, Scala.js, Scala Native, Android, and iOS.

## Repository layout

- `core/`: low-level cross-platform providers APIs.
- `engines/`: Screen2D and GameObject engines, built on top of `core`.
- `modules/` and `extensions/`: optional features such as Scene2D, particles, and Tiled.
- `backends/`: platform backends for Android, Desktop JVM, Scala Native, and HTML5.
- `bazel/`: public build rules, generated platform entry points, and asset tooling.
- `examples/`: runnable integration examples.
- `website/`: end-user documentation.

## Main commands

```bash
bazel build //...
bazel test //...
```
