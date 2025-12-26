apply(plugin = "net.fabricmc.fabric-loom")

repositories {
    mavenCentral()
}

dependencies {
    add("minecraft", "com.mojang:minecraft:${rootProject.ext["minecraft_version"]}")

    // For @Environment annotations
    implementation("xland.mcmodbridge:fabric-distmarker:0.1.0")

    api("xland.mcmod:enchlevel-langpatch:3.1.0")
    api("net.fabricmc:sponge-mixin:0.16.5+mixin.0.8.7")
    api("io.github.llamalad7:mixinextras-common:0.5.0")
}

sourceSets {
    main {
        resources.srcDir("src/generated/resources")
    }
}