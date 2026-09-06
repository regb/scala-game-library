# Release builds run R8 with shrinking and obfuscation. SGL and game classes
# are referenced statically, so R8 traces them without extra keep rules. Add
# targeted keep rules here only for classes your game loads reflectively.
#
# The Scala standard library references optional JDK classes that the Android
# SDK does not provide; silence those instead of keeping the dead code.
-dontwarn scala.**
