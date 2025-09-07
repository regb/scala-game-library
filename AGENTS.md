# Scala Game Library (SGL) - Agent Guide
This is the sources of the Scala Game Library (SGL), a cross-platform library for game development in Scala.
It leverages Scala mutli-compiler and multi-platform support, in particular the standard JVM backend, the ScalaJS backend, and the Scala Native backend.
It also supports Android through the JVM integration, and iOS with a web view.

## Code Organization
- `core/`: Cross-platform game logic and abstract interfaces
- `desktop-awt/`, `desktop-native`, `html5/`, `android/`: Platform-specific implementations
- `examples/`: Sample games showing usage patterns. Game code typically extends `GameApp` trait and implements screen-based architecture.
- `bazel/`: Rules to suppor cross-platform scala builds as well as to provide a framework to users for building games with SGL.

## Build System
This project uses **Bazel** as the build system.

## Technologies
* Scala 2.13
* scalatest 3.2
* Bazel

## Commands
- Build all: `bazel build //...`
- Run tests: `bazel test //...` 

Make sure to ALWAYS run build and test whenever you make some changes.

## Code Style
- **Package structure**: Use reverse domain notation (`com.regblanc.sgl.hello.core`)
- **Imports**: Group by package, use selective imports (`import sgl.{GraphicsProvider, AudioProvider}`)
- **Traits**: Use self-types for dependencies (`this: GraphicsProvider with SystemProvider =>`)
- **Naming**: CamelCase for classes/traits, camelCase for methods/fields
- **Documentation**: Use ScalaDoc with `/** */` for public APIs
- **Error handling**: Use Option/Try, avoid exceptions in game logic
- **Architecture**: Cake pattern with Provider/Component traits for cross-platform code DI

