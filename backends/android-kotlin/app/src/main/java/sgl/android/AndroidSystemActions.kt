package sgl.android

import sgl.InputActions

internal object AndroidSystemActions {
    // Back$() calls the public Scala object constructor, creating a different
    // instance. Use the singleton that Scala games register and compare against.
    val Back = InputActions.`Back$`.`MODULE$`
}
