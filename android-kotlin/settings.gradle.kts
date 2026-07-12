pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "SGL Android Toolkit"
include(":sgl-android")
project(":sgl-android").projectDir = file("app")
include(":sgl-android-firebase")
project(":sgl-android-firebase").projectDir = file("firebase")
include(":sgl-android-admob")
project(":sgl-android-admob").projectDir = file("admob")
include(":sgl-android-play-games")
project(":sgl-android-play-games").projectDir = file("play-games")
