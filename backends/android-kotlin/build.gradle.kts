// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Analytics 24 uses Kotlin 2.4 metadata. Raise AGP's built-in Kotlin dependency.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false

    id("com.google.gms.google-services") version "4.4.3" apply false
}