plugins {
    id("net.fabricmc.fabric-loom")
}

loom {
    accessWidenerPath = file("../common/src/main/resources/creaturesandbeasts.accesswidener")
}

base {
    archivesName = "${rootProject.property("mod_name")}-Fabric"
}

sourceSets {
    main {
        java.srcDirs("../common/src/main/java", "src/main/java")
        resources.srcDirs("../common/src/main/resources", "src/main/resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
    implementation("maven.modrinth:geckolib:${rootProject.property("geckolib_version")}")

    implementation("org.infernalstudios:config:${rootProject.property("infernalstudios_config_version")}")
    implementation("com.electronwill.night-config:core:${rootProject.property("nightconfig_version")}")
    implementation("com.electronwill.night-config:toml:${rootProject.property("nightconfig_version")}")
    include("org.infernalstudios:config:${rootProject.property("infernalstudios_config_version")}")
    include("com.electronwill.night-config:core:${rootProject.property("nightconfig_version")}")
    include("com.electronwill.night-config:toml:${rootProject.property("nightconfig_version")}")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        properties(listOf("fabric.mod.json"),
            "mod_version" to rootProject.property("mod_version"),
            "minecraft_version" to rootProject.property("minecraft_version"),
            "geckolib_version" to rootProject.property("geckolib_version"),
            "fabric_loader_version" to rootProject.property("fabric_loader_version"),
            "fabric_api_version" to rootProject.property("fabric_api_version"),
            "architectury_version" to rootProject.property("architectury_version")
        )
    }
}
