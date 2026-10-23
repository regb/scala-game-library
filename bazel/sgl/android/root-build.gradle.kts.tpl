// Analytics 24 uses Kotlin 2.4 metadata. Raise AGP's built-in Kotlin dependency.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.android.library") version "9.2.1" apply false
    id("com.github.triplet.play") version "4.1.1" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}
