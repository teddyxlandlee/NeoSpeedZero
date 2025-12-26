pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        gradlePluginPortal()
    }
}

plugins {
    // This plugin allows Gradle to automatically download arbitrary versions of Java for you
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "neospeedzero"

include("common")
include("fabric")
include("neoforge")
