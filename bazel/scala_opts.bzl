"""Shared Scala compiler options."""

# Scala 3 compiler options. Keep these warning-oriented during the migration so
# targets can be ported incrementally without turning hygiene findings into
# hard failures.
SGL_SCALACOPTS = [
    "-release:8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Wunused:all",
]
