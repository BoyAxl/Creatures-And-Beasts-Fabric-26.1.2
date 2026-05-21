package com.cgessinger.creaturesandbeasts.fabric;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.ModLoaderProxy;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.util.BiomeSpawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class CreaturesAndBeastsFabric implements ModInitializer, ModLoaderProxy {

    @Override
    public void onInitialize() {
        var instance = new CreaturesAndBeasts(this);

        var spawns = new BiomeSpawns();
        instance.addSpawns(spawns);

        FuelValueEvents.BUILD.register((builder, context) -> builder.add(CNBItems.CINDERSHELL_SHELL_SHARD.get(), 6400));

        int i = 0;
        for (BiomeSpawns.SpawnData spawn : spawns.getSpawns()) {
            BiomeModifications.create(CreaturesAndBeasts.id("mob_spawn_" + i++))
                .add(ModificationPhase.ADDITIONS, selection -> spawn.selector().test(selection.getBiomeHolder()), (selection, modification) -> {
                    modification.getMobSpawnSettings().addSpawn(spawn.category(), spawn.spawnerData().toSpawnerData(), spawn.spawnerData().weight());

                    if (spawn.spawnCosts() != null) {
                        modification.getMobSpawnSettings().addMobCharge(spawn.spawnerData().type(), spawn.spawnCosts().charge(), spawn.spawnCosts().energyBudget());
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
