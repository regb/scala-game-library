# Bazel dependency setup and updates

SGL owns its platform/tooling Maven dependencies and commits their resolved
versions, URLs and SHA-256 checksums in `third_party/maven_install.json`.
Games do not need a copy of that lockfile or declarations for SGL's audio,
JSON, Scala.js, Scala Native, LWJGL or test libraries.

## Game setup

Keep the SGL pin and the root Scala-version selection:

```starlark
bazel_dep(name = "sgl.bzl", version = "0.0.0", repo_name = "sgl")
git_override(
    module_name = "sgl.bzl",
    commit = "<SGL commit>",
    remote = "https://github.com/regb/scala-game-library.git",
)
bazel_dep(name = "rules_scala", version = "7.2.6")
scala_config = use_extension(
    "@rules_scala//scala/extensions:config.bzl", "scala_config",
)
scala_config.settings(scala_version = "3.3.8")
```

Remove repeated SGL Maven coordinates and the game's `scala_deps.scala()`
call. Remove `rules_jvm_external` too unless the game uses it for its own
libraries. SGL already enables the Scala toolchain. Root Scala settings remain
necessary because rules_scala 7.2.6 ignores non-root version settings and
otherwise selects Scala 2.12.21.

A BUILD macro cannot configure Bzlmod. `MODULE.bazel` cannot load an ordinary
setup macro, and an extension cannot rewrite another extension's tags. The
standard module configuration is sufficient for Maven ownership. A future
SGL-only consumer module would need upstream Scala configuration support or a
separately validated custom toolchain, not just a wrapper macro.

## Repository ownership

SGL uses the distinctly named `sgl_maven` install and imports it locally as
`maven`. Existing SGL BUILD files and macros can keep using `@maven` without
sharing the game's resolution namespace. Games do not need to import SGL's
Maven repository.

A game may use its own default `maven` install for genuinely game-specific
libraries, with its own lockfile. Separate graphs do not automatically align
versions. Avoid introducing another version of a dependency already used by
SGL. Review compile/runtime classpaths for duplicate coordinates and classes
when adding libraries, particularly Scala standard libraries.

An advanced game can deliberately configure the same `sgl_maven` name in the
standard Maven extension. This merges contributions and gives the root
control over settings and the lockfile. Omitting the root lockfile clears
SGL's lock selection, so this is not the recommended default. Arbitrary game
additions require a newly resolved root graph/lock; an unchanged SGL lock is
not sufficient. Multiple non-root lockfiles for the same name are an error
unless the root selects one.

Shared resolution can be useful for applications needing one unified version
graph. It trades that alignment for root-owned repinning. Separate repositories
can share checksum-addressed downloads, but that does not deduplicate Java
classes or targets. SGL's existing desktop graph already includes two copies
of Scala 2.13.18's standard library through the toolchain and lift-json. This
migration does not change those versions or remove that pre-existing overlap.

Keep one SGL lock initially to align its platform dependencies. With pinned
fetching, unrelated artifacts need not be downloaded for a desktop target.
Splitting platform/test locks would add repin effort and cross-graph conflicts
without a demonstrated need. SGL's ScalaTest toolchain enablement remains
unchanged in this migration.

## Updating dependencies upstream

From the SGL root:

```sh
# Edit artifacts in MODULE.bazel, then resolve and update the committed lock.
REPIN=1 bazel run @maven//:pin

# Run without REPIN to enforce input signatures.
bazel query @maven//:all
bazel build //...
bazel test //...
tools/check-consumer-deps
```

`@maven` is SGL's local alias for `sgl_maven`. Pinning updates
`third_party/maven_install.json` directly. Do not hand-edit its signatures or
checksums. Review added/removed coordinates, transitive versions, classifier
coverage, download URLs and SHA-256 changes. Investigate changed bytes at
unchanged coordinates. Commit declarations and lockfile together.

CI explicitly validates the lock as SGL root and builds a temporary consumer's
desktop and HTML5 applications with no Maven configuration. It repeats those
consumer builds with `--nofetch`.

In rules_jvm_external 7.0, non-root lockfiles work, but
`fail_if_repin_required` is propagated only from root install tags. Consumer
success alone does not validate a changed declaration against the lock. The
root check is required. `MODULE.bazel.lock` records module/extension metadata,
not a replacement for the Maven artifact lock.

## Downloads and caches

Pinned builds create checksum-bearing Bazel `http_file` repositories instead
of repeatedly running Coursier graph/POM resolution. The `unpinned_sgl_maven`
repository remains available for repinning, but normal pinned builds need not
fetch it. First builds still download artifacts and toolchains.

rules_scala separately fetches compiler/toolchain artifacts from its checked-in
coordinate/checksum tables. Protobuf owns its own default Maven graph and
lockfile. Java, Go, protoc, Tiled and Android SDK/Gradle downloads remain
separate. A Maven lock does not make the whole build network-independent.

CI uses Bazel's repository cache across workspaces. Keep that cache separate
from action caching. For an optional local shared cache, set
`build --repository_cache=<your-cache-directory>` in a user Bazel rc file.
No machine-specific path belongs in SGL or a game's committed configuration.
Do not seed caches manually as a prerequisite for building.

Bazel 9.1.1 exposes `--experimental_repository_downloader_retries` with a
default of 5. Retain the supported default rather than adding guessed Coursier
environment settings to pinned builds. Optional trusted mirrors must preserve
expected checksums. Test the actual locked download URLs or Bazel downloader
rewrite configuration; changing an install's repository list does not by
itself rewrite artifact URLs already stored in the lock.

The original Rat Trap failures were intermittent Coursier connection refusals.
Their network root cause remains unproven. Neither this migration nor an
endpoint switch guarantees network reliability. TLS and checksum verification
remain enabled, and no local proxy is required.

## Validation

Implementation starts from SGL `6cf9ed2`. The regenerated lock removes eight
protobuf-related artifact groups from SGL's graph, adds LWJGL EGL and missing
LWJGL platform classifiers, and changes no checksum for an existing artifact.
Protobuf retains its own lock rather than contributing to SGL's install.

Completed locally with Bazel 9.1.1:

- Strict root lockfile query passed.
- `bazel build //...` passed, including Native compilation and HTML5 linking.
- The minimal consumer built desktop and HTML5 without Maven declarations,
  then passed repeated builds with repository fetching disabled.
- `bazel test //...` passed 11 of 13 test targets. Two EGL integration targets
  failed to locate host `libEGL.so.1`. Both fail identically on unmodified
  upstream `6cf9ed2`; this is not introduced by the dependency migration.
- A fresh desktop-AWT example build passed with isolated output-user-root,
  repository cache, repository-contents cache and Coursier cache locations,
  and action disk caching disabled. It fetched 12 SGL artifact JAR repositories,
  none for Native or ScalaTest. No unpinned Maven repository was fetched and
  no Coursier cache was created. Scala toolchain downloads remained separate.
- Repeating that desktop build with `--repository_disable_download --nofetch`
  passed. This verifies cache reuse, not future network availability.

For a cold-cache reproduction, use new empty directories for every named cache:

```sh
cold="$(mktemp -d)"
bazel --output_user_root="$cold/bazel" build //examples/snake:desktop-awt \
  --repository_cache="$cold/repository" \
  --repo_contents_cache="$cold/contents" --disk_cache= \
  --repo_env=COURSIER_CACHE="$cold/coursier" \
  --repo_env=COURSIER_JVM_CACHE="$cold/coursier-jvm"
```

A fresh output root with a warm global repository cache is not a cold-cache
test. Android also needs separate SDK/Gradle cache testing. No game releases
or tags are part of this migration.
