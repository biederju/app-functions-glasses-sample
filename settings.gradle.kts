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
        google() {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\..*")
            }
        }
        mavenCentral()
        maven("https://androidx.dev/snapshots/builds/12476373/artifacts/repository") {
            content {
                includeGroupByRegex("androidx\\..*")
            }
        }
    }
}

rootProject.name = "app-functions-glasses-sample"
include(":app")
 