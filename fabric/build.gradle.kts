apply(plugin = "net.fabricmc.fabric-loom")

dependencies {
    add("minecraft", "com.mojang:minecraft:${rootProject.ext["minecraft_version"]}")

    api(project(":common"))

    implementation("net.fabricmc:fabric-loader:${rootProject.ext["fabric_loader_version"]}")
    implementation("net.fabricmc.fabric-api:fabric-api:${rootProject.ext["fabric_api_version"]}")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

