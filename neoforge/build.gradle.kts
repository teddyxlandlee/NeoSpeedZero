plugins {
    id("net.neoforged.moddev") version "2.0.138"
}

dependencies {
    api(project(":common"))
}

neoForge {
    version = rootProject.ext["neoforge_version"].toString()
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}
