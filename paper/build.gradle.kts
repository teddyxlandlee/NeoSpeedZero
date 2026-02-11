plugins {
    `java-library`
}

apply(plugin = "net.fabricmc.fabric-loom")

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
    }
}

dependencies {
    add("minecraft", "com.mojang:minecraft:${rootProject.ext["minecraft_version"]}")
    // Temporarily use 1.21.11 Paper API as a bridge, since
    // CraftBukkit implementations are currently unavailable
    api("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":common"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}
