plugins {
    id("net.fabricmc.fabric-loom")
}

fun prop(name: String) = rootProject.property(name) as String

loom {
    accessWidenerPath = file("../common/src/main/resources/creaturesandbeasts.accesswidener")
}

base {
    archivesName = "${prop("mod_name")}-Fabric"
}

sourceSets {
    main {
        java.srcDirs("../common/src/main/java", "src/main/java")
        resources.srcDirs("../common/src/main/resources", "src/main/resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")
    implementation("maven.modrinth:geckolib:${prop("geckolib_version")}")

    implementation("org.infernalstudios:config:${prop("infernalstudios_config_version")}")
    implementation("com.electronwill.night-config:core:${prop("nightconfig_version")}")
    implementation("com.electronwill.night-config:toml:${prop("nightconfig_version")}")
    include("org.infernalstudios:config:${prop("infernalstudios_config_version")}")
    include("com.electronwill.night-config:core:${prop("nightconfig_version")}")
    include("com.electronwill.night-config:toml:${prop("nightconfig_version")}")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        properties(listOf("fabric.mod.json"),
            "mod_version" to project.version,
            "minecraft_version" to prop("minecraft_version"),
            "minecraft_version_range" to prop("minecraft_version_range"),
            "geckolib_version" to prop("geckolib_version"),
            "fabric_loader_version" to prop("fabric_loader_version"),
            "fabric_loader_version_range" to prop("fabric_loader_version_range"),
            "fabric_api_version" to prop("fabric_api_version"),
            "fabric_api_version_range" to prop("fabric_api_version_range")
        )
    }
}
