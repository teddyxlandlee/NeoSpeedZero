import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

plugins {
    `java-library`
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("com.modrinth.minotaur") version "2.+"
}

allprojects {
    group = rootProject.ext["maven_group"]!!
    version = rootProject.ext["mod_version"]!!

    repositories {
        maven("https://maven.hixland.com") {
            name = "Teddy's Maven"
        }
        maven("https://maven.fabricmc.net")
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        withSourcesJar()
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
}

listOf("jar", "sourcesJar").forEach { taskName ->
    configurations.create("universalShadowCandidate_${taskName}") {
        isCanBeResolved = true
        isCanBeConsumed = false
        dependencies.add(dependencyFactory.create(files(
            subprojects.map { p -> p.tasks.named(taskName) }
        )))
    }
}

// Shadow jar
private fun subprojectArchives(taskName: String) : Provider<Iterable<Configuration>> =
    configurations.named("universalShadowCandidate_${taskName}").map(::listOf)

tasks.register<ShadowJar>("shadowJar") {
    description = "Shadowing submodules into a universal jar"
    configurations.set(subprojectArchives("jar"))
    archiveClassifier.set("universal")

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()

    manifest.attributes(
        "Implementation-Version" to project.version     // used by Paper port's data pack cache
    )
}

tasks.register("shadowSourcesJar", ShadowJar::class) {
    description = "Shadowing sources of submodules into a sources jar"
    configurations.set(subprojectArchives("sourcesJar"))
    archiveClassifier.set("universal-sources")
}

tasks.build {
    dependsOn("shadowJar")
    dependsOn("shadowSourcesJar")
}

extensions.getByName<com.modrinth.minotaur.ModrinthExtension>("modrinth").apply {
    loaders.set(providers.gradleProperty("mr_loaders").map { it.split(',') })
    token.set(providers.environmentVariable("MR_TOKEN"))
    projectId.set(providers.gradleProperty("mr_project_id"))
    versionNumber.set(provider {
        "${project.version}+${providers.gradleProperty("minecraft_version").get()}-universal"
    })
    versionName.set(provider {
        providers.gradleProperty("mr_version_name_format").get().format(
            Locale.ENGLISH,
            providers.gradleProperty("mr_version_game_range").get(),
            "Universal",    // loader display
            providers.gradleProperty("mr_version_mod_abbr").get(),
            project.version
        )
    })

    changelog.set(providers.environmentVariable("MR_CHANGELOG").map {
        it.removePrefix("[ci publish] ").trim()
    })
    versionType.set(providers.gradleProperty("mr_version_type"))
    gameVersions.set(providers.gradleProperty("mr_version_game").map { it.split(',') })
    detectLoaders.set(false)
    autoAddDependsOn.set(false)

    dependencies {
        optional.project("enchlevel-langpatch")
    }

    uploadFile.set(provider { tasks["shadowJar"] })
    additionalFiles.add(provider { tasks["shadowSourcesJar"] })    // shadowSources

    debugMode.set(providers.environmentVariable("MR_DEBUG_MODE").map { "1" == it }.orElse(false))
}
