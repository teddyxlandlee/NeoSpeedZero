plugins {
    id("net.neoforged.moddev") version "2.0.140"
}

dependencies {
    api(project(":common"))
}

neoForge {
    enable {
        version = rootProject.ext["neoforge_version"].toString()
        isDisableRecompilation = false
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}
