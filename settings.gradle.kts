pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JavaLabsNSU"
include(":lab1")
include(":lab2")
include(":lab3")
include(":lab4")
include(":lab5-server")
include(":lab5-client")
