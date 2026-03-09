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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Cowork"
include(":app")
include(":users:ui")
include(":users:data")
include(":users:domain")
include(":providers:ui")
include(":providers:data")
include(":providers:domain")
include(":bookings:ui")
include(":bookings:data")
include(":bookings:domain")
include(":reviews:ui")
include(":reviews:data")
include(":reviews:domain")
