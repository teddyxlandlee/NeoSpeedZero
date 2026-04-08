plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
    }
}

dependencies {
    paperweight.paperDevBundle("26.1.1.build.+")
    implementation(project(":common")) {
        isTransitive = false    // we don't want those client-only code on classpath
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}
