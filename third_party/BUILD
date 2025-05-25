exports_files(["maven_install.json"])

# TODO: create a rule for this
# To update transitive dependencies, run `bazel run @maven//:pin` and copy
# the file over the maven_install.json
# We can actually run the following command to update inplac: `REPIN=1 bazel run @maven//:pin`
#
# It seems like the lock file is always the source of truth used for external deps, and the list
# of artifacts listed in maven.install is only read when running the :pin command, which
# can update the state of all dependencies.

