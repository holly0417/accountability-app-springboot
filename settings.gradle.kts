rootProject.name = "accountability"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/plugins-milestone")
        maven("https://repo.spring.io/plugins-snapshot")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}