plugins {
    `java`
    id("net.fabricmc.fabric-loom") version "1.16.2" apply false
}

val modVersion = System.getenv("GITHUB_TAG")?.removePrefix("v")
    ?: rootProject.property("mod_version") as String

allprojects {
    apply(plugin = "java")

    version = "${modVersion}+${rootProject.property("minecraft_version")}"
    group = rootProject.property("maven_group") as String

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.infernalstudios.org/releases")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
}
