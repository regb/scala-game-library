# Release builds run R8 with shrinking and obfuscation. SGL and game classes
# are referenced statically, so R8 traces them without extra keep rules. Add
# targeted keep rules here only for classes your game loads reflectively.
#
# The Scala standard library references optional JDK classes that the Android
# SDK does not provide; silence those instead of keeping the dead code.
-dontwarn scala.**

# SGL loads drawables through Resources#getIdentifier with names derived from
# asset paths (see AndroidGraphicsProxy.loadImage), which R8 cannot see, so
# all drawable resources must be kept from the resource shrinker as well.
-keep class **.R$drawable { <fields>; }
