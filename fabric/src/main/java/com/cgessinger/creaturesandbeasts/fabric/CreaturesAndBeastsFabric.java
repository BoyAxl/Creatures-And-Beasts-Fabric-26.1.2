package com.cgessinger.creaturesandbeasts.fabric;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.ModLoaderProxy;
import com.cgessinger.creaturesandbeasts.util.BiomeSpawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class CreaturesAndBeastsFabric implements ModInitializer, ModLoaderProxy {

    @Override
    public void onInitialize() {
        var instance = new CreaturesAndBeasts(this);

        var spawns = new BiomeSpawns();
        instance.addSpawns(spawns);

        int i = 0;
        for (BiomeSpawns.SpawnData spawn : spawns.getSpawns()) {
            BiomeModifications.create(CreaturesAndBeasts.id("mob_spawn_" + i++))
                .add(ModificationPhase.ADDITIONS, selection -> spawn.selector().test(selection.getBiomeRegistryEntry()), (selection, modification) -> {
                    modification.getSpawnSettings().addSpawn(spawn.category(), spawn.spawnerData());

                    if (spawn.spawnCosts() != null) {
                        modification.getSpawnSettings().setSpawnCost(spawn.spawnerData().type, spawn.spawnCosts().charge(), spawn.spawnCosts().energyBudget());
                    }
                });
        }

        instance.commonSetup();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
