import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

plugins {
    `java-library`
    id("net.fabricmc.fabric-loom") version "1.14-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("com.modrinth.minotaur") version "2.+" apply false
}

apply(plugin = "com.modrinth.minotaur")

val javaVersion = 25

allprojects {
    group = rootProject.ext["maven_group"]!!
    version = rootProject.ext["mod_version"]!!

    repositories {
        maven("https://mvn.7c7.icu") {
            name = "7c7 Maven"
        }
        maven("https://maven.fabricmc.net")
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        withSourcesJar()
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
}

private fun Configuration.withDependency(c: FileCollection) : Configuration {
    this.dependencies.add(dependencyFactory.create(c))
    return this
}

// Shadow jar
private fun subprojectArchives(taskName: String) : Iterable<Configuration> = subprojects.map { p ->
    val files : FileCollection = p.tasks.getByName<Jar>(taskName).outputs.files
    p.configurations.create("universalShadowCandidate_${taskName}_subproject_${p.name}").withDependency(files)
}

tasks.register("shadowJar", ShadowJar::class) {
    configurations.set(subprojectArchives("jar"))
    archiveClassifier.set("universal")

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()

    doFirst {
        println(configurations.get().map { it.files })
    }
}

tasks.register("shadowSourcesJar", ShadowJar::class) {
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
