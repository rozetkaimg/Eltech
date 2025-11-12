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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { setUrl("https://artifactory-external.vkpartner.ru/artifactory/maven/") }
        google()
        mavenCentral()
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
