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
        maven { setUrl("https://artifactory-external.vkpartner.ru/artifactory/maven/") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://artifactory-external.vkpartner.ru/artifactory/maven/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ePolitech"
include(":app")
include(":storage")
include(":domain")
include(":data")
include(":presentation")
include(":network")
include(":model")
